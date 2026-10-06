# 功能项交付追溯矩阵（四层：功能项 → 端点 → spec → 测试）

> **本文性质：占位骨架（draft），非甲方权威基线。**
>
> 当前甲方《功能项清单 / 需求规格说明书》**尚未到位**。按"先落地实现、拿数据再改"的约定，本矩阵以**已交付的 31 能力域 / 288 端点**为骨架先行落地，作为回填前的占位结构。
>
> - **第 1 列「功能项」采用后端能力域名作占位**；甲方清单到位后替换为权威功能项编号（如 `F-01`）/ 名称，并补充父子层级与对应等保条款（见 §2 待回填字段）。
> - **端点层（第 2–4 列）** 为脚本反推的权威计数（源：`scope-inventory.md` §1，可复现）。
> - **spec 层（第 5 列）** 为逐端点已计算的归属证据，详见 `scope-inventory.md` §2「能力域 spec」列；全量 **0 缺口**。
> - **测试层（第 6 列）** 由测试目录关键词扫描得出（源：`src/test/**/*.java`），总数为 **96 测试类 / 950+ 用例，全绿**；跨域基础设施测试见 §3。
>
> ⚠️ 禁止手抄端点明细：端点级证据以 `scope-inventory.md` 为唯一真源，本文只做层级归集与待回填标注。

## 1. 四层矩阵（按能力域，端点数降序）

| # | 功能项（占位 = 能力域） | 端点 | 读 | 写 | 能力域 spec（证据见 scope-inventory §2） | 测试类（估，见 §3） | 状态 |
| - | ----------------------- | ---: | -: | -: | ---------------------------------------- | ------------------- | ---- |
| 1 | emergency（应急） | 31 | 15 | 16 | ✅ 已归属（§2） | 1 | 占位 |
| 2 | system（系统管理） | 31 | 11 | 20 | ✅ 已归属（§2） | 10 | 占位 |
| 3 | security（安全事件/黑名单/周界） | 29 | 14 | 15 | ✅ 已归属（§2） | 17 | 占位 |
| 4 | rescue-resource（救援资源） | 20 | 8 | 12 | ✅ 已归属（§2） | 2 | 占位 |
| 5 | emergency-plan（应急预案） | 16 | 6 | 10 | ✅ 已归属（§2） | 2 | 占位 |
| 6 | fire-facility（消防设备） | 15 | 5 | 10 | ✅ 已归属（§2） | 2 | 占位 |
| 7 | tv（工业电视） | 15 | 10 | 5 | ✅ 已归属（§2） | 3 | 占位 |
| 8 | video（视频监控） | 14 | 8 | 6 | ✅ 已归属（§2） | 1 | 占位 |
| 9 | fire-monitoring（消防监控） | 12 | 6 | 6 | ✅ 已归属（§2） | 1 | 占位 |
| 10 | hazard（重大危险源） | 11 | 5 | 6 | ✅ 已归属（§2） | 1 | 占位 |
| 11 | communication（通讯设备） | 9 | 3 | 6 | ✅ 已归属（§2） | 2 | 占位 |
| 12 | production（生产报警） | 9 | 8 | 1 | ✅ 已归属（§2） | 2 | 占位 |
| 13 | auth（认证/鉴权） | 7 | 2 | 5 | ✅ 已归属（§2） | 4 | 占位 |
| 14 | dashboard（大屏分析） | 7 | 7 | 0 | ✅ 已归属（§2） | 2 | 占位 |
| 15 | emergency-event（应急事件） | 7 | 2 | 5 | ✅ 已归属（§2） | 2 | 占位 |
| 16 | typhoon-emergency（台风应急） | 7 | 4 | 3 | ✅ 已归属（§2） | 2 | 占位 |
| 17 | device（设备） | 5 | 2 | 3 | ✅ 已归属（§2） | 1 | 占位 |
| 18 | form-records（表单记录） | 5 | 2 | 3 | ✅ 已归属（§2） | 1 | 占位 |
| 19 | mgmt-ledger（管理台账） | 5 | 2 | 3 | ✅ 已归属（§2） | 2 | 占位 |
| 20 | special-operation（特殊作业） | 5 | 2 | 3 | ✅ 已归属（§2） | 1 | 占位 |
| 21 | alarm（报警） | 4 | 1 | 3 | ✅ 已归属（§2） | 5 | 占位 |
| 22 | fire-alarm（火警） | 4 | 1 | 3 | ✅ 已归属（§2） | 2 | 占位 |
| 23 | security-blacklist（安全黑名单） | 3 | 1 | 2 | ✅ 已归属（§2） | 2 | 占位 |
| 24 | fire-situation（火情） | 3 | 3 | 0 | ✅ 已归属（§2） | 2 | 占位 |
| 25 | map（地图 GeoJSON） | 3 | 3 | 0 | ✅ 已归属（§2） | 2 | 占位 |
| 26 | uplink（上行/审计） | 3 | 1 | 2 | ✅ 已归属（§2） | 3 | 占位 |
| 27 | drills（演练） | 2 | 2 | 0 | ✅ 已归属（§2） | 1 | 占位 |
| 28 | msds（MSDS） | 2 | 2 | 0 | ✅ 已归属（§2） | 1 | 占位 |
| 29 | tasks（任务） | 2 | 2 | 0 | ✅ 已归属（§2） | 1 | 占位 |
| 30 | accident-rescue（事故救援） | 1 | 1 | 0 | ✅ 已归属（§2） | 2 | 占位 |
| 31 | weather（气象） | 1 | 1 | 0 | ✅ 已归属（§2） | 2 | 占位 |
| — | **合计** | **288** | **140** | **148** | **31 域全覆盖，0 缺口** | **96 类 / 950+ 用例** | — |

> 注：第 6 列「测试类（估）」为按文件名关键词扫描的近似值，用于标识各域已有测试存在；精确覆盖率以 `mvn test` 报告与 `backend-test-baseline` spec 为准。跨域基础设施测试（鉴权链、脱敏、CORS、广播、Flyway 迁移、端到端）不计入单域，见 §3。

## 2. 待甲方《功能项清单》回填字段清单

甲方清单到位后，需在本矩阵补充/替换以下字段（建议以脚本或表格批量回填，勿手抄逐端点）：

1. **权威功能项编号**：如 `F-01`、`MOD-2.3`，替换第 1 列占位名。
2. **权威功能项名称**：替换能力域名（如 `emergency` → 甲方术语）。
3. **功能项父子层级**：模块 → 子功能 → 端点，用于生成甲方视角的树状追溯。
4. **对应等保 2.0 条款（第三级）**：每个功能项映射到的安全要求条目（如 8.1.2.1 身份鉴别）。
5. **功能项 ↔ 能力域映射确认**：甲方功能项与本项目 31 能力域的对应关系（一对多/多对一均可能），确认后回填第 1 列并锁定。
6. **验收口径**：甲方对该功能项的验收标准/用例指针。

> 回填完成后，本文由「占位骨架」升级为「甲方权威基线」，并回链到 `scope-inventory.md`（其 §0 已约定：甲方清单为上游真源，各条目须回链功能项编号）。

## 3. 测试覆盖扫描（目录反推，非逐端点）

**总量**：`src/test/**/*.java` 共 **96 个测试类**，约 **950+ 用例**，`mvn test` 全绿（门禁阈值 lines/funcs/branch 已达标）。

**按域代表测试类（估）**：

| 能力域 | 代表测试类（扫描自文件名） |
| ------ | --------------------------- |
| emergency | `EmergencyControllerTest` |
| system | `SystemUser/Role/Menu/Dict/ZoneControllerTest`、`SystemUser/Dict/Menu/RoleServiceTest`、`AccountServiceTest`（10） |
| security | `JwtUtil/AuthorizationService/JwtFilter/RequireAuthInterceptor/RoleAuthorityServiceTest`、`PasswordPolicy/LifecycleInterceptorTest`、`SystemAuthGranularityTest`、`DataScopeHelper/ResolverTest`、`HmacFilterTest`、`RateLimiter/RateLimitFilterTest`、`HardControlInterceptor/PathsTest`、`RealtimeAuthHandshakeInterceptorTest`、`TokenVersionServiceTest`（17） |
| rescue-resource | `RescueResourceControllerTest`、`RescueResourceAbacTest` |
| emergency-plan | `EmergencyPlanControllerTest`、`EmergencyPlanServiceTest` |
| fire-facility | `FireFacilityCollectorTest`、`FireFacilityMonitorReportTest` |
| tv | `TvControllerTest`、`TvServiceTest`、`TvCollectorTest` |
| video | `VideoControllerTest` |
| fire-monitoring | `FireMonitoringServicePatrolDateTest` |
| hazard | `HazardControllerTest` |
| communication | `CommDeviceControllerTest`、`CommRecordControllerTest` |
| production | `ProductionControllerTest`、`ProductionServiceTest` |
| auth | `AuthControllerTest`、`AuthServiceTest`、`AuthServiceMenuContractTest`、`AuthServiceRefreshTest` |
| dashboard | `DashboardControllerTest`、`DashboardServiceTest` |
| emergency-event | `EmergencyEventControllerTest`、`EmergencyEventServiceTest` |
| typhoon-emergency | `TyphoonEmergencyControllerTest`、`TyphoonEmergencyServiceTest` |
| device | `DeviceControllerTest` |
| form-records | `FormRecordServiceTest` |
| mgmt-ledger | `LedgerIdSupportTest`、`BusinessWriteServiceTest` |
| special-operation | `SpecialOperationControllerTest` |
| alarm | `AlarmControllerTest`、`AlarmServiceTest`、`AlarmAssemblerTest`、`AlarmSimulatorTest`、`AlarmWebSocketHandlerTest` |
| fire-alarm | `FireAlarmControllerTest`、`FireAlarmServiceTest` |
| security-blacklist | `BlacklistControllerTest`、`BlacklistServiceTest` |
| fire-situation | `FireSituationControllerTest`、`FireSituationServiceTest` |
| map | `MapControllerTest`、`MapServiceTest` |
| uplink | `UplinkControllerTest`、`UplinkServiceTest`、`SystemAuditHelperTest` |
| drills | `DrillControllerTest` |
| msds | `MsdsControllerTest` |
| tasks | `TaskControllerTest` |
| accident-rescue | `AccidentRescueControllerTest`、`AccidentRescueServiceTest` |
| weather | `WeatherControllerTest`、`WeatherServiceTest` |

**跨域基础设施测试（不计入单域）**：`ResultEnvelopeTest`、`CorsFilterTest`、`CorsConfigTest`、`SecurityBeansTest`、`IntegrationContractTest`、`IdNameCacheServiceTest`、`MaskUtilTest`、`MaskingSerializerTest`、`GlobalExceptionHandlerTest`、`TraceContextTest`、`RealtimePublisherTest`、`RealtimeBroadcastServiceTest`、`DbLayerIntegrationIT`、`PostgresqlFlywayMigrationIT`、`EndToEndFlowTest`、`WorkstationServiceTest`、`FireMonitoringServicePatrolDateTest`。

**测试基线 spec**：`openspec/specs/backend-test-baseline/spec.md`（定义门禁阈值与分层策略）；`openspec/specs/backend-security-baseline/spec.md`（安全测试基线）。

## 4. 计数总览（复用 scope-inventory §1，可复现）

- 控制器：**37** 个；端点映射：**288**（读 140 / 写 148）；契约域：**31**。
- 三项交叉校验：**0 缺口**（契约未登记 0 / 前端零引用 0 / 写端点授权 148/148）。
- spec 覆盖：**31 域全覆盖，0 缺口**；测试：**96 类 / 950+ 用例全绿**。

> **重新生成 / 回填**：端点或 spec 有增删后，先 `python scripts/gen-scope-inventory.py` 重跑并回填 `scope-inventory.md`；本文第 2–4 列与第 5 列随之自动对齐（引用 §2），无需手抄。甲方清单到位后仅改第 1 列与 §2 待回填字段。
