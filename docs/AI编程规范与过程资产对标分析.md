# AI 编程规范 · 过程文档 · 流程说明 —— 对标分析与优化建议

> 📍 **存放位置说明**：本报告已纳入 `backend-scaffold/docs/`。文中所有文件路径（如 `backend-scaffold/...`、`frontend-scaffold/...`）均为**相对于仓库根目录 `mm-security-platform`** 的路径；读者应从仓库根按路径寻址。

> **对比对象**：`safety-training-system`（下称 **sts**，来自 `safety-training-system-main-…zip`）
> **基准项目**：`mm-security-platform`（下称 **mm**，本仓库 `backend-scaffold` + `frontend-scaffold` 双库）
> **分析轴**：AI 编程规范（AGENTS/CLAUDE/.cursor）、过程文档（docs/engineering）、流程说明（OpenSpec/路线图/QA·Retro）
> **定位**：在仓库既有《脚手架与安全教育培训平台对标分析.docx》(2026-09-02) 的「平台能力」对标之外，补一层「AI 协作治理」专项对标。sts 是本次的**标杆参照**，但 mm 在多个维度已经反超。

---

## 0. 一句话结论

mm 的 AI 编程骨架（L0–L4 分级 / OpenSpec 四件套 / 契约先行 / QA·Retro 闭环 / `.cursor` harness）**已经与 sts 同级**，且在**安全红线、UI/品牌红线、契约自动化守门、多方言 Flyway、perf 基线**上**更强**；真正落后的只有三类：

1. **后端横向设计决策文档偏薄**（缺 auth-design / password-security / audit-log / data-masking / optimistic-lock 等）；
2. **DB 集成测试层（zero-DB + DB-IT）尚未落地**（mm 自己把 `*IT` 显式延后了）；
3. **部署 / 客户环境 / 产品基线文档缺位**（sts 有 7 篇部署类 + 功能项.csv，mm 仅 `backend-scaffold/docs/deployment/README.md` 1 篇 + 等甲方功能项）。

其余差异（QA/Retro 篇数、Ship 目录）基本是「项目更年轻、Change 更少」带来的**数量差，非结构性缺口**——只要保持既有纪律，随 Change 累积自然会补齐。

---

## 1. 维度对标矩阵

判定图例：✅ 持平 ｜ 🟢 mm 更强 ｜ 🔴 mm 偏弱 ｜ 🟡 结构差异（非优劣）

| # | 维度 | sts（标杆） | mm（现状） | 判定 |
|---|------|------------|-----------|------|
| 1 | 分级工作流 L0–L4 | AGENTS §1.1 决策树 | AGENTS §1 决策树 + 四门槛 | ✅ 持平 |
| 2 | 规则优先级仲裁 | 5 级仲裁链 | 5 级仲裁链（含跨库特例） | ✅ 持平 |
| 3 | OpenSpec 四件套 + 归档 + CI 卫生 | opsx-* 命令 + 归档纪律 | + `check-openspec-hygiene.mjs` 门禁；**实际已归档：后端 6 / 前端 50+**，specs 22+ | 🟢 mm 更实 |
| 4 | 契约先行 + 机器可读 | `docs/api/` 18 个 openapi.json + 四条铁律 | 前端库 12 openapi + **生成 TS 类型** + `check-api-contract.mjs` + 跨库四同步 | 🟢 mm 更自动 |
| 5 | 分层 AGENTS（根/端/子） | 根 + backend/frontend/docs | 根 + apps/mgmt·mobile/screen/subapps + docs（三端更细） | 🟢 mm 更细 |
| 6 | `.cursor` harness | 6 cmd + 3 rule + 6 skill | 双库各 6 cmd + 3 rule + 6 skill（api-contract / core / ui-tokens-theme…） | ✅ 持平 |
| 7 | 长期路线图（阶段依赖） | `docs/requirement/end-to-end-development-roadmap.md`（10 阶段） | `backend-scaffold/docs/architecture/roadmap.md`（7 阶段·跨库） | ✅ 持平（组织不同） |
| 8 | 产品/需求基线（功能项） | `功能项.csv` + 需求规格说明书 + 74 条 FR 对照 | `backend-scaffold/docs/requirement/scope-inventory.md` 仅代码派生，**等甲方功能项回填** | 🔴 mm 缺口 |
| 9 | 架构/横向设计决策文档 | **~30 篇**（含 auth-design / password-security / audit-log / data-masking / optimistic-lock / id-name-cache / button-permission / module-boundary / business-architecture…） | **5 篇**（domain-model / filter-chain / README / roadmap / test-strategy） | 🔴 mm 明显偏薄 |
| 10 | 安全/审计专项文档 | auth-design / password-security / audit-log / data-masking / super-admin-bypass | filter-chain + 前端 auth-token（**有硬控红线但缺设计文档**） | 🔴 mm 缺设计层 |
| 11 | 数据库变更双轨（Flyway） | init 完整 SQL + V 增量 + baseline（仅 MySQL） | V1 快照 + V 增量（**h2/postgresql/dameng 三方言**）+ 双轨策略 | 🟢 mm 更强 |
| 12 | 测试策略（两层） | zero-DB `*Test` + DB `*IT`（Testcontainers MySQL，164 例）**全落地** | zero-DB 基线 + `*IT` **显式延后**（待 Testcontainers） | 🔴 mm 最大工程缺口 |
| 13 | 部署 / 客户环境文档 | 7 篇（customer-environment-questionnaire / on-prem briefing / OSS-CDN 成本…） | `backend-scaffold/docs/deployment/README.md` 仅 1 篇 | 🔴 mm 缺口 |
| 14 | QA / Retro 记录量 | qa 111 + retro 82 | 后端 ~8 + 前端 ~7 | 🟡 数量差（年龄导致） |
| 15 | Ship / 发布检查文档 | `backend-scaffold/engineering/ship/` 目录存在 | 无 ship 目录 | 🟡 阶段 7 前需建 |
| 16 | UI / 品牌红线（三端） | design-system + clean-modern-style-baseline | **4 篇 UI 规范**（大屏/后台/移动/门户边界）+ 中石化品牌红线 §6.7 | 🟢 mm 更强 |
| 17 | 安全硬控红线 | 以 auth / password 为主 | **零下行控制 / B3 / 20 位 MDM / 防重放 / 硬控拦截**（领域特有） | 🟢 mm 更强 |
| 18 | 性能基线 / SLA | 无 | `docs/perf/`（性能基线 / 弱 GPU 验收 / SLA 对齐） | 🟢 mm 更强 |
| 19 | git-identity 后端隔离（lims） | ✅ monorepo 内按 `git user.name=lims` 隔离 | 双库天然隔离（无需） | 🟡 结构差异 |
| 20 | 仓库结构 | monorepo（单根 AGENTS） | 双独立 git 仓库（跨库四同步） | 🟡 结构权衡 |

---

## 2. mm 相对 sts 的领先项（必须守住，勿回退）

这些是目前 mm 的护城河，任何「向 sts 看齐」的优化都**不应削弱**它们：

- **三端 UI / 中石化品牌红线**：`UI规范-{大屏/后台/移动/门户边界}.md` + AGENTS §6.3–§6.7，密度与可执行性高于 sts 的 design-system。
- **安全硬控红线**：零下行控制、`B3` 包络、`20 位 MDM` 设备编码、防重放签名、`HardControlInterceptor`——这是安全管控系统的领域本质，sts（教培系统）没有对等概念。
- **契约守门自动化**：`scripts/check-api-contract.mjs`（路由+schema 双层）+ `npm run gen:api-types` + CI 卫生检查，比 sts 的「四条铁律人工遵守」更不易漂移。
- **多方言 Flyway**：h2 / postgresql / dameng 三套迁移同版本对齐，sts 仅 MySQL。
- **perf 基线与 SLA**：`docs/perf/` 已立，sts 无。
- **OpenSpec 实际活跃度**：mm 已归档 50+ 前端 Change、10+ capability spec，证明流程在真跑，而非只写在 AGENTS 里。

---

## 3. 重点优化项（按优先级）

### P0 — 低风险高收益，建议本周内补

**P0-1　补齐后端横向设计决策文档（对齐 sts 的 architecture 套件）**
mm 后端 `docs/architecture/` 只有 5 篇，而 sts 有 ~30 篇。最该先写的是**安全与数据正确性**这一组（mm 是安全系统，这部分最该有「设计真源」）：
- `auth-design.md`（JWT 无状态 + 刷新令牌 Cookie 化 + RBAC 模型，呼应前端 `auth-token.md`，但补服务端决策）
- `password-security.md`（BCrypt、密钥仅环境变量、占位密钥拒绝启动）
- `audit-log.md`（操作审计埋点 + 脱敏规则，呼应前端 Uplink 审计）
- `data-masking.md`（日志/响应脱敏，现仅 filter-chain 提了一句）
- `optimistic-lock.md` + `id-name-cache.md`（并发与缓存一致性护栏）
- `module-boundary.md`（controller/service/mapper/entity 职责红线，现散在 AGENTS §6.2 表，抽成独立文档更易被 AI 检索）
> 做法：把现有 `backend-scaffold/docs/architecture/filter-chain.md` 作为样板，每篇固定「决策 / 现状 / 约束 / 反模式」四段；不必一次写 30 篇，先写上面 6 篇。

**P0-2　加一个「仓库根 AI 入口」伞文件**
当前根目录只有「长期共识文档」（系统事实，非 AI 指令），**没有根级 AGENTS.md**。AI 从仓库根启动时读不到 umbrella 指引。建议二选一：
- 在根新建 `AGENTS.md`，链接：两端 `backend-scaffold/AGENTS.md` + `backend-scaffold/docs/architecture/roadmap.md` + 契约真源 `frontend-scaffold/docs/api/` + 跨库四同步 §11；或
- 在「长期共识文档」顶部加一节「AI 协作入口」，指向上述文件。
> 目的：消除「根无 AI 入口」缝隙，让任意工具（Claude/Cursor/Codex/WorkBuddy）在根目录就能拿到全局约束。

**P0-3　把「阶段 7 生产就绪」所需文档先立骨架**
mm 路线图阶段 7 = 达梦 DM8 实测 / prod 联调 / 部署演练 / 安全渗透复核。现在 `deployment/` 只有 README。建议先建：
- `backend-scaffold/docs/deployment/customer-environment-questionnaire.md`（客户环境问卷，sts 有现成可参考）
- `backend-scaffold/docs/deployment/dameng-migration-runbook.md`（达梦实测迁移手册，复用现有 `backend-scaffold/src/main/resources/application-dm.yml` + `backend-scaffold/db/migration/dameng`）
- `backend-scaffold/docs/deployment/penetration-checklist.md`（渗透复核清单）
> 不阻塞开发，但阶段 7 一启动就有文档承载，避免临时补。

### P1 — 工程能力，阶段 6/7 前必须落地

**P1-1　落地 DB 集成测试层（mm 当前最大工程能力缺口）**
mm 后端 AGENTS §2.2 明写「`*IT` 在引入 Testcontainers 后再启用」，sts 已全量落地（zero-DB `*Test` + DB `*IT`，164 例，schema 复用 init 完整 SQL + Flyway baseline）。建议：
- 引入 Testcontainers（或本地 H2 之外的真实 PG 容器）跑带 DB 测试；
- 明确分层：`*Test`（standalone MockMvc + Mockito）答「逻辑/契约」，*`IT`（最小 MyBatis 切片）答「真实 SQL/落库/逻辑删除/乐观锁」；
- **禁止在测试目录复制第二份 DDL**——schema 必须复用 `backend-scaffold/db/migration/**` 的 V 文件（mm 已有三方言 V，正合适）；
- 交付时如实报告 IT 是否执行（mm AGENTS 已要求，保持即可）。
> 这是「mm 不照搬 sts 全量金字塔」与「mm 必须有真实落库验证」之间的正确落点：不追求 162+16 例规模，但**必须有** DB-IT 这一层。

**P1-2　把「路线图 + 进度台账 + 进行中 Change」读动作固化进 `.cursor/rules/`**
sts 有 `end-to-end-development-roadmap.mdc` 作为 harness 约束，mm 目前只靠 `roadmap.md` 文字 + AGENTS 提及。建议在后端 `backend-scaffold/.cursor/rules/` 加 `backend-scaffold/.cursor/rules/end-to-end-roadmap.mdc`，强制：L3/L4 开工前先读 roadmap → 进度台账 → 当前 Change，且不得为后续阶段建占位实现。与现有 `backend-scaffold/.cursor/rules/backend-scaffold-core.mdc` 同级。

### P2 — 治理密度，随 Change 累积

**P2-1　QA/Retro 保持「即刻记录 + 证据必附」**
mm 已采用四段式 Retro 与「证据是结论必要附件」，与 sts 同款。差距只是篇数（年龄导致）。保持纪律即可，无需特意补历史。

**P2-2　建立 `backend-scaffold/engineering/ship/` 目录与发布检查模板**
sts 有 ship 目录（即便当前 0 文件）。mm 阶段 7 部署前需要：代码检查 / 迁移 / 环境配置 / 部署步骤 / 回滚方案 五段式。可在 `backend-scaffold/templates/` 加 `backend-scaffold/templates/_ship_template.md`（mm 已有 `_qa_template`/`_retro_template`，补齐 ship 即可）。

**P2-3（可选）monorepo 化或根级 umbrella**
sts 单根 monorepo 的优势是「一个根 AGENTS 统领」。mm 双库带来跨库四同步摩擦。两条路：
- 轻量：用 P0-2 的根伞文件 + 跨库四同步脚本缓解（推荐，零重构）；
- 重量：合并为 monorepo（大决策，需用户拍板，本分析不默认建议）。
> mm 的双库结构在「契约真源唯一在前端库」上有清晰理由，不视为缺陷，仅作权衡提示。

---

## 4. 落地清单（可直接勾选）

- [x] **P0-1** 后端 `backend-scaffold/docs/architecture/` 新增 6 篇：auth-design / password-security / audit-log / data-masking / optimistic-lock / id-name-cache（+ module-boundary）✅ 已落地（2026-09-08）
- [x] **P0-2** 根目录新增 `AGENTS.md`（或给「长期共识文档」加「AI 协作入口」节），链接两端 AGENTS + roadmap + 契约真源 ✅ 已落地（根 `AGENTS.md` umbrella）
- [x] **P0-3** `backend-scaffold/docs/deployment/` 新增 customer-environment-questionnaire / dameng-migration-runbook / penetration-checklist ✅ 已落地
- [x] **P1-1** 引入 Testcontainers，落地 `*IT` 层，复用 `backend-scaffold/db/migration/**` V 文件，禁第二份 DDL ✅ 已落地（`DbLayerIntegrationIT`，H2+Flyway 复用 V1–V8；Testcontainers 因本机无 Docker 改为 H2 集成 DB，生产方言待真实实例复核）
- [x] **P1-2** 后端 `backend-scaffold/.cursor/rules/` 新增 `end-to-end-roadmap.mdc`，固化 L3/L4 开工前读路线图 ✅ 已落地
- [x] **P2-2** `backend-scaffold/templates/` 新增 `_ship_template.md`，`backend-scaffold/engineering/ship/` 建目录 ✅ 已落地
- [ ] **待甲方** 收到《功能项清单》后回填 `scope-inventory.md` 的需求追溯列（roadmap §4 已挂账）

---

## 5. 附：两端关键资产对照（便于直接取用）

| 资产 | sts 路径 | mm 对应路径 |
|------|---------|------------|
| 总 AI 规则 | `AGENTS.md`（989 行） | `backend-scaffold/AGENTS.md` + `frontend-scaffold/AGENTS.md` |
| Claude 入口 | `CLAUDE.md` | 双库 `CLAUDE.md` |
| Cursor harness | `.cursor/{commands,rules,skills}` | 双库 `.cursor/{commands,rules,skills}`（已齐） |
| 路线图 | `docs/requirement/end-to-end-development-roadmap.md` | `backend-scaffold/docs/architecture/roadmap.md` |
| 需求基线 | `docs/product/功能项.csv` | `backend-scaffold/docs/requirement/scope-inventory.md`（待甲方清单） |
| 契约真源 | `docs/api/*.openapi.json`（18） | `frontend-scaffold/docs/api/*.openapi.json`（12 + 生成类型） |
| 架构文档 | `docs/architecture/*`（~30） | `backend-scaffold/docs/architecture/*`（5）🔴 |
| 部署文档 | `docs/deployment/*`（7） | `backend-scaffold/docs/deployment/README.md`（1）🔴 |
| 测试策略 | zero-DB + DB-IT 全量 | zero-DB 基线 + DB-IT 延后 🔴 |
| QA/Retro | qa 111 / retro 82 | 后端 ~8 / 前端 ~7 🟡 |

> 标注 🔴 的是本次建议的优化重点；🟡 为年龄导致的数量差，靠纪律自然补齐。
