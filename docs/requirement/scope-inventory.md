# 需求交付基线 · 交付范围追溯清单（后端）

> 本文是后端**已交付能力**的内部追溯基线，由代码与契约真源（`frontend-scaffold/docs/api/*.openapi.json`）派生。
> 若甲方提供独立《功能项清单 / 需求规格说明书》，其应作为**上游真源**，本文各条目须回链到对应功能项编号（参照项目 `docs/product/功能项.csv` + proposal「本次覆盖的功能项」表做法）。
> 任何新增端点必须在此登记一行，并标注其归属 capability spec 与前端消费模块，否则视为未闭环。

## 0. 口径

- **端点计数**：契约真源 12 个 openapi 文件中 `gis.openapi.json` 为外部网关前瞻桩（不实现），其余 11 个共 **35 条路径** = **34 个 REST 端点 + 1 个 WebSocket 推送通道**（`/ws/alarm`）。
- **capability spec**：归属见 `openspec/specs/<capability>/spec.md`（已回填归档闭环）。
- **前端模块**：指 `frontend-scaffold/src/` 下消费该端点的 service / store / 页面域。

## 1. 交付范围 × 能力域 × 前端模块 追溯矩阵

| # | 端点 (method path) | 控制器 | 能力域 (spec) | 前端消费模块 |
| - | ------------------ | ------ | ------------- | ------------ |
| 1 | POST /api/v1/auth/login | AuthController | backend-security-baseline | services/auth.ts（login） |
| 2 | POST /api/v1/auth/refresh | AuthController | backend-security-baseline | services/auth.ts（refresh，凭 HttpOnly Cookie） |
| 3 | POST /api/v1/auth/logout | AuthController | backend-security-baseline | stores/auth.ts（logout 清 Cookie） |
| 4 | GET /api/v1/auth/me | AuthController | backend-security-baseline | stores/auth.ts（当前用户，读 sys_user 真实数据） |
| 5 | GET /api/v1/auth/menus | AuthController | backend-security-baseline | router/menu.ts（RBAC 动态菜单，5 个顶部 fm-*） |
| 6 | GET /api/v1/alarms | AlarmController | alarm-domain | services/alarm.ts（列表） |
| 7 | POST /api/v1/alarms | AlarmController | alarm-domain | services/alarm.ts（建警，服务端 RBAC 硬控） |
| 8 | PUT /api/v1/alarms/{alarmId} | AlarmController | alarm-domain | stores/alarm.ts（ack/dispatch 沿 ACK_FLOW 前进） |
| 9 | DELETE /api/v1/alarms/{alarmId} | AlarmController | alarm-domain | stores/alarm.ts（闭环 CLOSED） |
| 10 | GET /api/v1/dashboard/overview | DashboardController | dashboard-analytics | services/dashboard.ts |
| 11 | GET /api/v1/dashboard/alarm-trend | DashboardController | dashboard-analytics | services/dashboard.ts（趋势，add-dashboard-alarm-trend） |
| 12 | GET /api/v1/dashboard/workstations | DashboardController | dashboard-analytics | services/dashboard.ts |
| 13 | GET /api/v1/dashboard/risk-heatmap | DashboardController | dashboard-analytics | services/dashboard.ts |
| 14 | GET /api/v1/devices | DeviceController | map-geojson | services/device.ts（设备 GeoJSON） |
| 15 | GET /api/v1/devices/{code} | DeviceController | map-geojson | services/device.ts（20 位 MDM 编码解析） |
| 16 | GET /api/v1/emergency/strength | EmergencyController | emergency-reference | services/emergency.ts（应急力量，读 sys_emergency_strength） |
| 17 | GET /api/v1/emergency/closed-cases | EmergencyController | emergency-reference | services/emergency.ts（结案） |
| 18 | GET /api/v1/emergency/duty | EmergencyController | emergency-reference | services/emergency.ts（值班，读 sys_duty_member） |
| 19 | GET /api/v1/emergency/phones | EmergencyController | emergency-reference | services/emergency.ts（通讯录，读 sys_emergency_phone） |
| 20 | GET /api/v1/emergency/knowledge | EmergencyController | emergency-reference | services/emergency.ts（知识库，读 sys_knowledge_item） |
| 21 | GET /api/v1/fire-alarms | FireAlarmController | alarm-domain | services/fireAlarm.ts |
| 22 | GET /api/v1/hazards | HazardController | map-geojson | components/map/*（重大危险源） |
| 23 | GET /api/v1/hazards/{id} | HazardController | map-geojson | components/map/MajorHazardMapOverlay.vue |
| 24 | GET /api/v1/monitoring/points | HazardController | map-geojson | components/map/*（监测点位） |
| 25 | GET /api/v1/monitoring/alarms | HazardController | map-geojson | components/map/*（监测报警） |
| 26 | GET /api/v1/facilities/detail | HazardController | map-geojson | components/map/PlantAreaSelector.vue |
| 27 | GET /api/v1/map/alarms | MapController | map-geojson | services/map.ts（报警 GeoJSON） |
| 28 | GET /api/v1/map/devices | MapController | map-geojson | services/map.ts（设备 GeoJSON） |
| 29 | GET /api/v1/security/patrol-cameras | SecurityController | map-geojson | components/map/*（巡检摄像头） |
| 30 | GET /api/v1/security/gate-controls | SecurityController | map-geojson | components/map/*（闸口） |
| 31 | GET /api/v1/security/bollards | SecurityController | map-geojson | components/map/*（防撞柱） |
| 32 | GET /api/v1/security/search/vehicle | SecurityController | map-geojson | services/security.ts（车辆检索） |
| 33 | GET /api/v1/security/search/person | SecurityController | map-geojson | services/security.ts（人员检索） |
| 34 | GET /api/v1/security/events | SecurityController | map-geojson | services/security.ts（安防事件） |
| 35 | POST /api/v1/audit/log | UplinkController | uplink-audit | services/audit.ts（路由审计埋点） |
| 36 | POST /api/v1/field-reports | UplinkController | uplink-audit | apps/mobile（防爆手机现场回传，落库 fac_field_report） |
| 37 | WS /ws/alarm (topic: alarm.push) | AlarmWebSocketHandler | alarm-domain | services/ws.ts（RealtimeClient 消费推送） |

> 注：第 37 行为 WebSocket 通道，不计入 REST 34 端点，但属契约 `realtime.openapi.json` 约定的推送协议（`{topic, payload}` 包络 + 15s ping 心跳）。

## 2. 能力域交付状态（来自 openspec 归档）

| 能力域 spec | 状态 | 关键交付 |
| ---------- | ---- | -------- |
| backend-security-baseline | ✅ 已归档 | CORS fail-fast、密钥环境变量、越权 `@RequireAuth` + `AuthorizationService`、refresh Cookie 化、/auth/me/menus 真实数据源 + RBAC |
| backend-test-baseline | ✅ 已归档 | standalone MockMvc + 纯 Mockito，**155 单测全绿**，JaCoCo 行覆盖 0.80 门禁 |
| db-flyway-migration | ✅ 已归档 | Flyway 双轨（V1 快照 + V 增量），dev=H2 可跑；V6 现场回传落库 / V7 菜单 RBAC 字段 / V8 应急 4 参考表 |
| observability-probes | ✅ 已归档 | `/actuator/health` + `/actuator/prometheus`（免鉴权，限内网）+ 集成契约探针测试 |
| alarm-domain | ✅ 已归档 | 报警 CRUD + ACK_FLOW 状态机 + WS 推送 topic/payload 包络 |
| dashboard-analytics | ✅ 已归档 | 总览/趋势/热力图/工作站 4 端点 |
| emergency-reference | ✅ 已归档 | 应急力量/通讯录/知识库/值班 4 项由硬编码迁 DB 参考表 |
| map-geojson | ✅ 已归档 | 设备/报警 GeoJSON、重大危险源、监测点位、安防检索 |
| uplink-audit | ✅ 已归档 | 审计埋点 + 现场回传真落库（reporter 服务端覆盖 + 越权守门） |

## 3. 已知债务（须回链功能项后闭环）

- **认证/RBAC 域无独立 capability spec**：`/auth/*` 5 端点散落在 `backend-security-baseline` 内，未单独抽 `auth-rbac` spec。若甲方功能项将「统一认证 / 动态菜单 / 四层权限」列为独立条目，应补该 spec 并回链。
- **Prometheus 指标端点未入 spec**：`/actuator/prometheus` 在 security-baseline 之外交付，应在 `observability-probes` 补 Requirement 或独立 `observability-metrics` spec。
- **甲方《功能项清单》缺失**：本文为内部派生基线；一旦甲方提供需求文档，须逐条回链编号，建立「功能项 → 端点 → spec → 测试」四层可追溯。

## 4. 维护规则

- 新增端点：先建/归属 capability spec（L3/L4）→ 写契约 → 在此表追加一行 → 补单测 → `check-api-contract.mjs --strict` 通过。
- 端点下线：spec 标 Deprecated → 此表标注下线日期 → 契约移除 → 前端同步。
- 本文与 `openspec/specs/*`、`frontend-scaffold/docs/requirement/README.md`（业务域与权限模型）互为补充：本文是「后端交付了什么」，前端文档是「业务怎么跑」。
