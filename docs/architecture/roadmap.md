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
  - 🟨 **P0 · ABAC zone 注入收紧（代码侧已就绪，只差产品映射规则）**：WS 鉴权 + 三态 fail-open 骨架 09-17 已落地。
    - 🚨 **2026-10-06 纠正一个长期误判**：此前一直按「给约 30 个含 location 字段的**实体**实现 `ZoneAware`」推进，**这个思路大部分是无效的**——`RealtimeSyncAspect` 用 `@AfterReturning(returning="ret")` 取的是**写方法的返回值**，而 46 个广播域的写方法绝大多数返回 DTO / View，只有 `device` 域直接返回 `FacDevice`。对实体接线根本进不了切面。
    - ✅ **已按正确对象接线**（Change `2026-10-06-abac-zone-aware-dto-wiring`）：给 **18 个写方法返回 DTO** 接线 `ZoneAware`——16 个走 `getLocation()`（AlarmItem / PatrolExecutionView / CommunicationDevice / EmergencyEventItem / EmergencyCaseItem / FireAlarmItem / FireFacilityLedgerItem / ProductionAlarmItem / FireBrigadeTeam / RescueVehicleItem / RescueEquipmentItem / PersonSearchDetail / GateControlItem / PerimeterAlarmDetail / SpecialOperationItem / VideoCameraItem），2 个走 `getZoneName()`（BollardItem / TvMonitorSummary）。新增 `ZoneAwareDtoWiringTest`（5 例）锁「字段不漂移」与「未映射必 fail-open」。
    - **仍待产品**：`location → 防区` 语义规则，填入 `abac.zone-mapping.location-to-zones`（dev 现有 6 条示例：厂区南门 / 厂区西门 / 码头区 / 乙烯装置区 / 芳烃罐区 / 特勤保障区）。填了即生效、无需改代码。
    - **已知限制（fail-open，非缺陷）**：返回 `void` / `DeleteResult` 的删除类写方法无法携带防区；`SystemUserItem.zoneCodes` 为多值而 `ZoneAware` 只支持单值，故 `system.user` 域不接线（避免错误收紧）。
    - **行为保证**：未配置或未命中映射 → 事件 `zones == null` → 推给全部已认证会话，与现状**完全一致**；ALL 用户始终不受影响。
  - ⬜ **P1 · Testcontainers 方言 IT**（2026-10-05 实测订正，旧表述「本机无 Docker / 仅 1 个 IT」已失真）：`org.testcontainers` 已在 `pom.xml`，`src/test/java/.../integration/` 下已有 **6 个 IT**——PG：`PostgresqlFlywayMigrationIT` / `MgmtLedgerSqlPostgresqlIT`（`assumeTrue(DockerClientFactory.isDockerAvailable())`）；达梦：`DamengFlywayMigrationIT` / `MgmtLedgerSqlDamengIT`（`assumeTrue(DAMENG_JDBC_URL)`）；另 `DbLayerIntegrationIT` / `MgmtLedgerListIntegrationIT`。surefire 已配置 `<include>**/*IT.java</include>`，故 `mvn test` 会带跑（无环境时跳过而非失败）。
    - ~~**当前阻塞 = Docker daemon 未运行**~~ → **2026-10-05/06 已解决**：CLI-auth 代理 400 用 `api.version=1.44` 钉版本绕过；镜像拉取 `EOF` 真根因是 Windows 系统代理 `ProxyEnable=0`（Docker Desktop 4.70+ 不认 `daemon.json` 的 `proxies`），已在伞根 `ensure-docker-proxy.ps1` 固化自愈。达梦走本机实例（`D:\dameng` `localhost:5236`，非容器），另需 `DAMENG_JDBC_URL` + `-Pdm` + 本地驱动 jar + SYSDBA 口令。
    - ✅ **2026-10-06 00:01 三方言真机 IT 全部全绿，本项不再是缺口**：H2 6/6、PG 2/2（`Successfully applied 104 migrations → v107`）、达梦 2/2（V1–V107 全迁移，schema 107）。原「V58–V104 在 PG / 达梦上从未真机验证」的风险面**已清零**；后续每改迁移仍须三方言都重跑（H2 通过 ≠ PG / 达梦通过）。达梦三条硬限制见根 `AGENTS.md §5`。
  - ⬜ **需求追溯列**：`docs/requirement/scope-inventory.md` 的**端点矩阵部分已于 2026-10-06 由脚本全量重建**（37 Controller / 288 端点 / 31 契约域，见该文 §0–§2），不再手工维护；**仍阻塞**的是甲方《功能项清单》输入——一旦提供须逐条回链编号，建立「功能项 → 端点 → spec → 测试」四层追溯。
  - ✅ **能力域 spec 覆盖缺口：196/288 → 0/288（2026-10-06 已清零，本项关闭）**。根因是**已归档 Change 的 `spec-delta.md` 从未合并进 `openspec/specs/<capability>/spec.md`**（74 个归档 Change 中 70+ 个带 delta，但 fire-facility / video / hazard 等域连目录都没有）——**不是需求缺失**。两批回填：`2026-10-06-spec-delta-backfill`（新建 11 个 capability + 补 `emergency-reference`，196 → 66）与 `2026-10-06-spec-delta-backfill-batch2`（新建 `device` + 补 10 个既有 spec，66 → **0**），现 **31 个契约域全覆盖**。详见 `docs/requirement/scope-inventory.md §3`。
    - **新增纪律（防止再累积）**：① 归档 Change 时必须把 `spec-delta.md` 合并进 `openspec/specs/<capability>/spec.md`；② spec 内必须写**显式端点路径**，写 `{resource}` 占位符追溯脚本识别不到（首批 rescue-resource 曾因此漏 9 条）。
- **后端 `openspec/changes/` 当前无进行中 Change**（仅 `README.md` 归档纪律说明）；前端 `openspec/changes/` 同样为空。任何新的 L3/L4 工作须先经 openspec 新建 Change 并回填 `tasks.md`，再据此实施（双库各自归属）。
- **已补（不再阻塞）**：
  - ✅ 认证/RBAC 域独立 capability spec：`openspec/specs/auth-rbac/spec.md`（从 `auth-design.md` 抽取，与 `backend-security-baseline` 互补，不重复）。
  - ✅ prometheus 指标端点已入 `observability-probes` spec（`### Requirement: Prometheus 指标端点`）。
