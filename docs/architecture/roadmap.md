# 跨库端到端开发路线图（backend-scaffold ⇄ frontend-scaffold）

> 本文件是**跨库长期路线真相源**：只规定能力依赖顺序与阶段完成判据，不替代 openspec（实施范围仍以当前人工确认的 `openspec/changes/<name>/tasks.md` 为准）。进度记录在后端 `engineering/plans/end-to-end-development-progress-tracker.md` 与前端 `engineering/plans/end-to-end-development-progress-tracker.md`。

## 1. 阶段主线（按依赖顺序）

| # | 阶段 | 内容 | 状态 |
| --- | --- | --- | --- |
| 1 | 基础与契约底座 | 双库骨架、B3 包络、错误码分段、`/api/v1` 契约真源（前端库 `docs/api/*.openapi.json`）、跨库对拍脚本 | ✅ 完成 |
| 2 | 安全与鉴权加固 | JWT 无状态、防重放签名、CORS 白名单、硬控拦截、生产配置基线、零依赖单测基线 | ✅ 完成（`harden-backend-baseline` 已归档） |
| 3 | 契约债清零 | 前端契约已声明的端点后端实现回填：报警 CRUD、应急参考数据、地图点位、审计/现场回传、热力图、报警趋势 | ✅ 完成（`implement-remaining-contracts` 等已归档） |
| 4 | 可观测性 | Actuator 探针（health/liveness/readiness/info）、`/actuator/prometheus` | ✅ 完成（探针 Change 已归档；prometheus 系 Change 外交付，spec 待回填） |
| 5 | 生产数据基线 | Flyway 双轨迁移（h2/postgresql/dameng 三方言）、覆盖率门禁（jacoco 0.80）、CI/CD、容器化 | ✅ 完成（`feat-flyway-prod-migration` 已归档） |
| 6 | 业务域纵深 | 按前端业务域推进端到端闭环：监测预警确认流 → 应急指挥 → 设备/硬控读数 → 态势统计真实化 → 系统管理 | ⬜ 按 Change 逐个推进 |
| 7 | 生产就绪 | 达梦 DM8 实测迁移、prod profile 联调、部署演练、安全渗透复核 | ⬜ 未开始（达梦依赖环境） |

## 2. 阶段完成判据（硬性）

1. **端到端闭环**：接口链路从认证到落库全真实实现；**Mock、桩服务、占位页面、单层代码、建表/枚举完成，不得作为该阶段「已完成」的依据**。
2. **回归全绿**：后端 `mvn test`（含 jacoco 覆盖率门禁）0 failure；前端 `npm test`（vitest）0 failed、`npm run type-check` 0 错。
3. **契约对拍零漂移**：`node scripts/check-api-contract.mjs --strict` 通过；契约改动走四同步（openspec → 前端契约 → 后端实现 → 前端重生成类型）。
4. **文档同步**：`docs/` 与 `openspec/specs/` 反映最新事实；Change 全勾必归档（CI 卫生检查守门）。
5. **双轴 Review**：规格符合性 + 代码质量，结论三选一（通过/需修改/需人工决策）。

## 3. 执行规则

1. L3 / L4 开工前必读：本路线图 → 进度台账 → 当前已确认 Change（双库 `CLAUDE.md` §8 同口径）。
2. 前一阶段未形成真实纵向闭环时，不得仅为铺页面、建空表或占模块而提前开发后续阶段；人工调整优先级时仍须按 L3/L4 流程确认对应 Change。
3. 统计、热力、趋势类端点必须读取稳定、可追溯的业务事实；基础事实未稳定时不得虚构统计口径。
4. 每完成一个已确认 Task：先回填 `tasks.md` 勾选框，再更新进度台账（状态/证据/阻塞项/日期）。
5. 本路线图只登记阶段与能力状态，不得扩写成 `openspec/` 之外的第二套业务 Task。

## 4. 当前活跃工作（每次会话先读这里）

> 阶段 1–5 已全绿归档。下一处真实推进落在阶段 6（业务域纵深）。

- **进行中（前端库 `openspec/changes/`，跨库归属）**：
  - `mgmt-redesign-migration` / `mgmt-tabstrip-style-align` / `remaining-modules-inline-closed-loop` — 系统管理域 mgmt 子应用闭环
  - `screen-mock-to-service` — 大屏从 mock 切真实后端服务
  - `wujie-subapp-fullscreen` / `wujie-subapp-switch-race` — wujie 微前端子应用体验
- **未开始（阶段 7）**：达梦 DM8 实测迁移、prod profile 联调、部署演练、安全渗透复核（依赖达梦环境与 release 窗口）。
- **后端 `openspec/changes/` 当前无进行中 Change**（仅 `README.md` 归档纪律说明）；后端下一工作须先经 L3/L4 新建 Change 并回填 `tasks.md`，再据此实施。
- **已知债务**（不阻塞阶段 6，但新 Change 须顺带清）：
  - 认证/RBAC 域尚无独立 capability spec（当前散落于 `backend-security-baseline` + `alarm-domain` 的确认流约束里），新做鉴权/权限类 Change 时补 `auth-rbac` spec。
  - prometheus 指标端点系 Change 外交付，未入 `observability-probes` spec，待补。
  - 若甲方提供《功能项清单》，回填 `docs/requirement/scope-inventory.md` 的需求追溯列。
