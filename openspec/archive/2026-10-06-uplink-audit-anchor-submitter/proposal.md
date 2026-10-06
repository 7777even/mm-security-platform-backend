# Proposal: 审计上报服务端锚定提交人（fac_audit_log.actor）

## 背景
渗透复核清单 §6 标记风险项：`POST /api/v1/audit/log` 仅 `@RequireAuth`（任意登录用户可提交），且
`UplinkService.reportAudit` 把客户端传入的 `action`/`module`/`detail`/`at` 原样落库，`fac_audit_log`
**无操作人字段**、不读 `UserContext` → 任意登录用户可写任意审计内容且**不可溯源**，等于可伪造审计记录。
同文件 `submitFieldReport` 已有"服务端覆盖身份"验证范式（`assertSelfOrAdmin` + `UserContext.username()`）。

## 决策
出决策文档 `docs/deployment/audit-anti-forgery-decisions.md` 给出 A/B/C 三方案对比，**用户已拍板方案 A**：
新增 `fac_audit_log.actor` 列，落库时由 `UserContext.username()` 服务端写入，复用 `submitFieldReport`
范式；不改 `@RequireAuth`、不加 `role=ADMIN`（不挡死普通用户审计上报）；契约 `AuditLogItem` 透出 actor、
前端审计页增"操作人"列。

## 目标
1. `fac_audit_log` 新增可空列 `actor VARCHAR(64)`（三方言 V108 迁移）。
2. `reportAudit` 由 `UserContext.username()` 服务端写入 `actor`；`username()` 为 null（`@RequireAuth` 下不应发生）防御性抛 `UNAUTHORIZED`。
3. `GET /api/v1/audit/log` 的 `AuditLogItem` 透出 `actor`；前端 `AuditView.vue` 增"操作人"列。
4. 门禁标记 `scripts/check-endpoint-authz.mjs` L48–51 `⚠️待安全确认` 更新为已处置说明。

## 非目标
- 不校验审计内容真伪（服务端无法独立验证"用户真点了哪个按钮"，前端是唯一观察者；内容溯源非本变更职责）。
- 不改变 `@RequireAuth` 登录态口径、不加 `role=ADMIN`。
- 不改动 `event_at` 客户端时间口径（仍由客户端上报、服务端不覆盖）。

## 人工确认关卡（L4）
- [x] 触及数据库结构（新增列）→ 已出提案
- [x] 方案选型（A/B/C）→ 用户拍板方案 A（`audit-anti-forgery-decisions.md`）
- [x] 等保口径（是否要求服务端溯源）→ 已写入决策文档 §7，默认按 A 实施满足"审计记录可归因于主体"
- [x] 不加 `role=ADMIN`、不挡死普通用户 → 确认保留 `@RequireAuth`
