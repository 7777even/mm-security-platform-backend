# 审计防伪造 · 收紧方案对比与决策（penetration-checklist §6）

> 关联：`docs/deployment/penetration-checklist.md §6`、`docs/architecture/audit-log.md`、`openspec/specs/uplink-audit/spec.md`、`scripts/check-endpoint-authz.mjs`（L48–51 标记 `⚠️待安全确认`）。
> 本文件是**决策输入**，结论须回链 §6 的 OpenSpec Change。状态：**已选方案 A 并实施（2026-10-06）**，OpenSpec Change `2026-10-06-uplink-audit-anchor-submitter` 已归档，渗透清单 §6 结论回填为「通过」。
> 事实基线（2026-10-06）：三方言迁移已真机全绿，当前最大迁移版本 V107（新增须 V108+）。

---

## 1. 现状与根因（代码事实）

| 事实 | 位置 | 说明 |
| ---- | ---- | ---- |
| 端点仅需登录 | `controller/UplinkController.java:30` 类级 `@RequireAuth` + `@PostMapping("/audit/log")` | 任意**已登录**账号可提交，无角色/归属约束（设计口径：每人操作都要被审计） |
| 内容原样落库 | `service/UplinkService.reportAudit` (L49–65) | `action`/`module`/`detail`/`at` 全部取客户端传入，`createdAt` 服务端写 |
| **无操作人字段** | `entity/FacAuditLog.java` 仅 `id/action/module/detail_json/event_at/created_at` | 审计行**不记录是谁上报的**——无任何溯源锚点 |
| 不读登录态 | 同上，`reportAudit` 不调用 `UserContext` | 与 `audit-log.md §3`「不得依赖 UserContext」一致，但该约束已与 `@RequireAuth` 实 enforcement 冲突（见 §5） |
| 查询也不含提交人 | `dto/AuditLogItem.java` + `queryAudit` | 管理端审计页（`AuditView.vue`）看不到"谁上报" |

**根因一句话**：审计行既无"提交人"锚点、内容又纯客户端断言 → 任意登录用户可写任意审计内容且无法溯源，等于可伪造审计记录。

**已存在的正确范式（可直接借鉴）**：`UplinkService.submitFieldReport`（L101–132）已实现"防身份冒用"——
`authorizationService.assertSelfOrAdmin(item.getReporter())` 后 `item.setReporter(UserContext.username())` 用服务端登录态**覆盖**客户端自报身份。审计缺的正是这一步。

---

## 2. 威胁模型（能伪造到什么程度）

- **A. 嫁祸/伪造内容**：登录用户可提交 `action="system.user.delete" module="ADMIN" detail="{目标:某人}"`，在审计表里制造"某人执行了某操作"的假记录。当前无任何字段能区分"系统真观测到"还是"某人编造"。
- **B. 掩盖行迹**：审计由前端埋点触发，服务端无法强制；攻击者不触发埋点即可让某操作"无痕"（客户端侧固有限制，任何方案都只能缓解、不能根除）。
- **C. 审计投毒/噪声**：批量灌入垃圾事件刷屏，干扰复盘。

关键判断：**服务端永远无法独立验证"用户真的点了哪个按钮"**——动作发生在客户端，前端是唯一观察者。因此对"内容真伪"做服务端校验本质是**治标**（只能保证格式合法，不能保证事实为真）。真正能落地的安全投资是**溯源（谁上报的）**——让每一行都可归因于一个真实登录，从而：
- 嫁祸（A）被瓦解：假记录明确写着"由攻击者 X 上报"，无法伪装成系统或他人生成；
- 投毒（C）可追责：垃圾行可定位到提交人 X，可 disciplinary 处理；
- 掩盖（B）是客户端固有限制，与本次服务端改动无关。

---

## 3. 方案对比

### 方案 A —— 服务端锚定提交人（**推荐**）

**做法**：新增可空列 `fac_audit_log.actor`，落库时由 `UserContext.username()` 服务端写入（**不**接受客户端传入的 actor）。`queryAudit`/`AuditLogItem` 透出，管理端新增"操作人"列。

- **改动点**：
  - 迁移：三方言各新增 `V108__audit_log_actor.sql`（`ALTER TABLE fac_audit_log ADD actor VARCHAR(64)`；H2 dev 已应用 V≤107 不可改，须新增 V108；PG/达梦同理新增）。
  - `FacAuditLog` 加 `actor` 字段（`@TableField("actor")`）。
  - `UplinkService.reportAudit`：`log.setActor(UserContext.username())`；防御性——若 `username()` 为 null（`@RequireAuth` 下不应发生）抛 `UNAUTHORIZED`。
  - `AuditLogItem` 加 `actor`；`toAuditItem` 映射。
  - 契约 `uplink.openapi.json`：`AuditLogItem` 增 `actor` 属性（描述"服务端按登录态写入，客户端不可伪造"）；**输入 `AuditEvent` 不加 actor 字段**（服务端覆盖，避免冗余往返）。
  - 前端：`npm run gen:api-types` 重生 `AuditLogItem` 类型；`AuditView.vue` 增 `el-table-column prop="actor" label="操作人"`。
  - 门禁：`scripts/check-endpoint-authz.mjs` L48–51 将 `⚠️待安全确认` 改为"已锚定提交人；self-report 内容语义保留，待甲方确认(选项③)"。
- **成本**：L3 变更（OpenSpec proposal + 2 仓提交 + 3 方言迁移 + 单测）。约 0.5–1 天，复用 `submitFieldReport` 范式，风险低。
- **残余风险**：客户端内容仍自我断言（见 §2 关键判断）——但每行可溯源，已满足等保"审计记录可定位到主体"的核心要求。
- **适用**：默认推荐。既修复"可伪造/不可溯源"，又不挡死普通用户审计上报（不加 `role=ADMIN`）。

### 方案 B —— 服务端白名单校验动作内容

**做法**：维护合法 `action` 字符串清单，拒绝未登记动作（`PARAM_INVALID`）；或给 `AuditEvent` 加客户端 `actor` 字段并在 Service 校验 `actor == UserContext.username()`。

- **问题**：
  - 前端跨 31 域会 emits 大量动作类型，中心化白名单**极易漏配**导致正常审计被拒，维护成本高、收益低；
  - 即便校验 actor 匹配，本质仍是"客户端声称 + 服务端比对"，不如方案 A 直接由服务端写入来得干净（B 的 actor 校验是 A 的退化形态）；
  - **不解决溯源**：白名单只保证格式，不记录"谁上报"，嫁祸/投毒仍不可追责。
- **成本**：更高（白名单维护 / 输入加字段 + 校验），价值更低。
- **结论**：不推荐；若执意要"内容约束"，其意图已被方案 A 的溯源覆盖且实现更优。

### 方案 C —— 接受当前自助上报语义（不改）

**做法**：保持现状，把"审计为前端自助上报、内容自我断言、不可溯源"作为**已接受风险**书面化。

- **风险**：伪造可演示、无溯源锚点；等保二级"安全审计"严格解读下，评估方可能认定审计记录不可靠（无法定位到操作主体）。
- **适用**：**仅当甲方/安全明确签字接受**此语义且等保评估不否决时。须写入 `engineering/retro/` 四段式风险登记 + 回链本 Change，作为显式 accepted-risk。
- **结论**：兜底选项，需甲方拍板，不可由研发单方面决定。

---

## 4. 推荐结论

**选 A**：它是方案 B（溯源意图）的干净实现，且契合等保"审计记录可归因于主体"的要求；不引入 `role=ADMIN`（不挡死普通用户）；改动小而复用已验证范式。方案 B 是治标且高维护，方案 C 需甲方显式接受风险。

> 若甲方确认"接受自助上报、不要求服务端溯源"（选项③），则走 C 并登记 accepted-risk；否则一律按 A 实施。

---

## 5. 两处必改的 collateral（无论选 A/C 都应修）

1. **`docs/architecture/audit-log.md §3` 约束已过时**：写"审计写入路径不得依赖 UserContext（上报可能来自匿名 uplink 通道）"，但 `UplinkController` 实际是类级 `@RequireAuth`，`UserContext` 在 `reportAudit` 路径**可靠可用**。应修订为"当前 `reportAudit` 受 `@RequireAuth` 保护，`UserContext` 可用；若未来开放匿名 uplink 通道再单独议定"。不修订会在实施 A 时与文档自相矛盾。
2. **`scripts/check-endpoint-authz.mjs` L48–51 标记**：选 A 后须将 `⚠️待安全确认` 更新为已处置说明（见 §3 方案 A 门禁项）。

---

## 6. 回链 OpenSpec Change 计划（待拍板后执行）

- Change：`2026-10-06-uplink-audit-anchor-submitter`（双仓）。
- 四同步：`uplink-audit` spec 增"提交人服务端锚定"需求 → 契约 `uplink.openapi.json`（`AuditLogItem.actor`）→ 后端实现 + `check-api-contract --strict` → `gen:api-types`。
- 测试：`UplinkServiceTest` 断言 `actor` 被置为当前登录用户、`UplinkControllerTest` 断言落库 `actor` 非空且不等客户端传入值（若后续加字段）。
- 渗透清单 §6：结论回填（通过 / 风险 / 阻塞）+ 回链此 Change。

---

## 7. 待拍板项（明确需要你/安全/甲方给结论）

- [ ] **选 A 实施 / 选 C 接受 / 其他**：默认建议 A。
- [ ] 若选 A：`actor` 列是否需要在管理端作为**可筛选列**（影响前端多一个过滤框）；默认仅展示不筛选。
- [ ] 等保口径：是否要求"审计记录必须服务端溯源"（决定 A 是否必需、C 是否可接受的底线）。
