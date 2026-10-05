# Capability: production-alarm（生产报警处置写回）

> 来源变更：2026-09-23-production-alarm-writeback（L4）
> 端点：`PUT /api/v1/production/alarms/{id}`
> 与消防报警 / 周界入侵告警写回对等，统一复用 `/ws/alarm` 实时总线。

## ADDED — 生产报警处置写回

- 系统 SHALL 提供 `PUT /api/v1/production/alarms/{id}`，对单条生产报警做处置状态流转或误报标记，与消防 / 周界报警写回对等（L4）。
- 请求体 SHALL 为局部更新（read-modify-write）：仅非空字段写入，未传字段保持不变。
  - `status` ∈ {`未处置`, `已确认`, `处置中`, `已处置`}（新增「已确认」态，区别于原 KPI 三态）。
  - `falseAlarm` ∈ {`是`, `否`, `未核实`}。
  - `handleResult` / `handleTime` / `dispatchPersonnel` / `notifyMethod` 为自由文本 / 逗号分隔串字段，不校验、不传不覆盖。
- 端点 SHALL 要求权限码 `production:ack`（V74 种子，BUTTON 级，授权 ADMIN / COMMANDER / SCHEDULER / TEAM_LEADER / INNER_OPER / OUTER_OPER 六类角色）；未授权返回 B3 `code=403`。
- 当 `id` 不存在时，SHALL 返回 B3 `code=404`（不抛 500）。
- 当枚举非法时，SHALL 返回 B3 `code=100`（参数非法）。
- 成功时 SHALL 落 `fac_production_alarm`（V73 新增 `version` 乐观锁列防护并发覆盖）并返回 `ProductionAlarmItem`（含 5 个回填字段，B3 包络），同时广播实时域 `production.alarm.changed`，复用统一 `/ws/alarm` 总线。

### Scenario: 确认生产报警

- **GIVEN** 一条 `status=未处置` 的生产报警 `id=1`
- **WHEN** 以 `{"status":"已确认"}` 请求 `PUT /production/alarms/1`
- **THEN** 该行 `status` 变为 `已确认`，响应 `code=0` 且 `data.status=已确认`，并广播 `production.alarm.changed`

### Scenario: 处置信息写回

- **GIVEN** 一条生产报警记录
- **WHEN** 以 `{"falseAlarm":"否","handleResult":"已现场处置并闭环","dispatchPersonnel":"张伟,李强","notifyMethod":"APP,SMS"}` 请求写回
- **THEN** 上述字段落库，其余字段（如 status）保持原值

### Scenario: 报警不存在

- **WHEN** 请求 `PUT /production/alarms/999999`
- **THEN** 返回 B3 `code=404`，不写库

### Scenario: 非法枚举

- **WHEN** 请求 `PUT /production/alarms/1`，`status` 为非枚举值
- **THEN** 返回 B3 `code=100`，不写库

### Scenario: KPI 待办聚合

- **GIVEN** 存在 `status=未处置` 与 `status=已确认` 两条生产报警
- **WHEN** 统计「未处置告警」
- **THEN** 两者合并计数（已确认仍属待办口径）

### Requirement: 生产域只读消费端点

系统 SHALL 提供生产域只读端点（登录即可）：`GET /api/v1/production/overview`（生产概览）、
`GET /api/v1/production/areas/{facilityId}`（装置区详情）、`GET /api/v1/production/devices`
（生产设备）、`GET /api/v1/production/personnel`（生产人员）、
`GET /api/v1/production/risk-warnings` 与 `/risk-warnings/{id}`（风险预警列表与详情）。

> **来源说明**：这 6 个只读端点在已归档 Change 中无 spec-delta 记录，本条按契约真源
> `docs/api/production.openapi.json` 与 `ProductionController` 实现反推。

响应统一 B3 包络（HTTP 200 + `code=0`）；只读端点不产生广播事件，未鉴权返回 401。

### Requirement: 生产报警关联抓拍

系统 SHALL 提供 `GET /api/v1/production/alarms/{id}/snapshots`，按生产报警 id 返回关联的
录像截图列表，使生产报警详情可精准内嵌关联抓拍。

> 抓拍本身由 `tv` capability 的 `POST /api/v1/tv/snapshots` 入库，
> 经可选 `alarmId` / `alarmType`（PRODUCTION / FIRE / PERIMETER）绑定到具体告警；
> 来源：`openspec/archive/2026-09-29-production-tv-snapshot-linkage/`。

该报警无关联抓拍时返回空列表（非空态兜底），不返回 404。

### 端点清单（production 只读，显式路径）

| Method | Path | 权限 |
| ------ | ---- | ---- |
| GET | `/api/v1/production/overview` | 登录即可 |
| GET | `/api/v1/production/areas/{facilityId}` | 登录即可 |
| GET | `/api/v1/production/devices` | 登录即可 |
| GET | `/api/v1/production/personnel` | 登录即可 |
| GET | `/api/v1/production/risk-warnings` | 登录即可 |
| GET | `/api/v1/production/risk-warnings/{id}` | 登录即可 |
| GET | `/api/v1/production/alarms/{id}/snapshots` | 登录即可 |
