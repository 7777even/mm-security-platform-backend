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

> 阶段 1–5 已全绿归档。阶段 6（业务域纵深）所列 Change 截至 2026-09-18 **已全部归档**——前端 `openspec/changes/` 为空，`mgmt-redesign-migration` / `mgmt-tabstrip-style-align` / `remaining-modules-inline-closed-loop` / `screen-mock-to-service` / `wujie-subapp-fullscreen` / `wujie-subapp-switch-race` 等均落在 `openspec/archive/`。→ 阶段 6 主体已闭环，下一处真实推进落在阶段 7 与下列已知债务 / 阻塞项。

- **部分完成（阶段 7 · 生产就绪）**：✅ 达梦 DM8 实测迁移已于 2026-10-06 真机跑通（V1–V107，2/2 IT 全绿）；⬜ 仍未开始：prod profile 联调、部署演练、安全渗透复核（依赖 release 窗口，骨架见 `docs/deployment/penetration-checklist.md`）。
- **真正未闭环的已知债务 / 阻塞项（按性质）**：
  - ⬜ **P0 · ABAC zone 注入收紧**：WS 鉴权 + 三态 fail-open 骨架 09-17 已落地（`ZoneAware` 标记接口 + `RealtimeBroadcastService` 按 `zone_codes` 三态过滤）。**待产品定 `location → 防区` 映射规则**后方可收紧；当前 fail-open = 不过滤，等同全量广播。
    - 2026-10-05 实测补齐阻塞点细节（避免误判为「只差填配置」）：代码链路**已完整可用**（`ZoneAware` → `ZoneMappingResolver` ← `AbacZoneMappingProperties` ← `RealtimeSyncAspect` → `RealtimeBroadcastService` 三态过滤），且 `application-dev.yml` 已带 6 条示例映射证明「实体 `getLocation()` → 防区 → 仅推同防区会话」闭环生效。
    - **但截至 2026-10-06 仅 `FacDevice` 1 个实体实现 `ZoneAware`**（2026-10-05 提交 `feat(device)`），其余约 30 个持有 `location`/`area` 字段的实体仍未实现 → 绝大多数写事件 `zones` 恒为 null → 即便生产填了映射也只有设备域有过滤效果。故产品侧仍需给两件事：① `location → 防区` 语义规则；② 指定哪些实体的哪个字段充当 location（由该实体实现 `ZoneAware` 暴露）。二者齐备才能真正收紧。
  - ⬜ **P1 · Testcontainers 方言 IT**（2026-10-05 实测订正，旧表述「本机无 Docker / 仅 1 个 IT」已失真）：`org.testcontainers` 已在 `pom.xml`，`src/test/java/.../integration/` 下已有 **6 个 IT**——PG：`PostgresqlFlywayMigrationIT` / `MgmtLedgerSqlPostgresqlIT`（`assumeTrue(DockerClientFactory.isDockerAvailable())`）；达梦：`DamengFlywayMigrationIT` / `MgmtLedgerSqlDamengIT`（`assumeTrue(DAMENG_JDBC_URL)`）；另 `DbLayerIntegrationIT` / `MgmtLedgerListIntegrationIT`。surefire 已配置 `<include>**/*IT.java</include>`，故 `mvn test` 会带跑（无环境时跳过而非失败）。
    - ~~**当前阻塞 = Docker daemon 未运行**~~ → **2026-10-05/06 已解决**：CLI-auth 代理 400 用 `api.version=1.44` 钉版本绕过；镜像拉取 `EOF` 真根因是 Windows 系统代理 `ProxyEnable=0`（Docker Desktop 4.70+ 不认 `daemon.json` 的 `proxies`），已在伞根 `ensure-docker-proxy.ps1` 固化自愈。达梦走本机实例（`D:\dameng` `localhost:5236`，非容器），另需 `DAMENG_JDBC_URL` + `-Pdm` + 本地驱动 jar + SYSDBA 口令。
    - ✅ **2026-10-06 00:01 三方言真机 IT 全部全绿，本项不再是缺口**：H2 6/6、PG 2/2（`Successfully applied 104 migrations → v107`）、达梦 2/2（V1–V107 全迁移，schema 107）。原「V58–V104 在 PG / 达梦上从未真机验证」的风险面**已清零**；后续每改迁移仍须三方言都重跑（H2 通过 ≠ PG / 达梦通过）。达梦三条硬限制见根 `AGENTS.md §5`。
  - ⬜ **需求追溯列**：`docs/requirement/scope-inventory.md` 的**端点矩阵部分已于 2026-10-06 由脚本全量重建**（37 Controller / 288 端点 / 31 契约域，见该文 §0–§2），不再手工维护；**仍阻塞**的是甲方《功能项清单》输入——一旦提供须逐条回链编号，建立「功能项 → 端点 → spec → 测试」四层追溯。
  - ⬜ **能力域 spec 覆盖缺口 196/288（2026-10-06 新登记）**：后端 `openspec/specs/*` 未提及的端点 196 条（68%），其中 11 个域 100% 缺口共 91 条。详见 `docs/requirement/scope-inventory.md §3`；不阻塞交付但阻塞甲方追溯，建议按域分批补（优先在既有 spec 增 Requirement，不主张为凑数新建 spec）。
- **后端 `openspec/changes/` 当前无进行中 Change**（仅 `README.md` 归档纪律说明）；前端 `openspec/changes/` 同样为空。任何新的 L3/L4 工作须先经 openspec 新建 Change 并回填 `tasks.md`，再据此实施（双库各自归属）。
- **已补（不再阻塞）**：
  - ✅ 认证/RBAC 域独立 capability spec：`openspec/specs/auth-rbac/spec.md`（从 `auth-design.md` 抽取，与 `backend-security-baseline` 互补，不重复）。
  - ✅ prometheus 指标端点已入 `observability-probes` spec（`### Requirement: Prometheus 指标端点`）。
