# emergency-event Specification

## Purpose

大屏应急指挥「应急事件」域数据源（`fac_emergency_event`，配 `fac_accident_incident`）：事件分组列表（只读）、登录态新增、登录态事件预警（报送）与登录态启动应急响应。新增写端点由 Change `2026-09-20-emergency-event-create` 回填；事件预警端点由 Change `2026-09-21-emergency-event-report` 回填；启动应急响应端点由 Change `2026-09-21-emergency-event-start-response` 回填；列表端点沿用既有 `GET /emergency-events`。

## Requirements

### Requirement: 应急事件分组列表（只读）

系统须提供 `GET /api/v1/emergency-events`，按 `scene`（`FIRE` 消防应急 / `PRELIMINARY` 先期处置）返回应急事件分组列表；`scene` 缺省返回全部（FIRE 在前）。数据源 `fac_emergency_event`，按 `scene + group_code` 聚合。

#### Scenario: 按场景查询

- **WHEN** `GET /api/v1/emergency-events?scene=FIRE`
- **THEN** B3 包络返回仅 FIRE 场景的分组列表（FIRE 在前）

#### Scenario: 缺省返回全部

- **WHEN** `GET /api/v1/emergency-events`
- **THEN** 返回全部场景分组（FIRE 在前、PRELIMINARY 在后）

### Requirement: 新增应急事件

系统须提供 `POST /api/v1/emergency-events`，接受 `EmergencyEventCreateRequest`，在**同一事务**内写入 `fac_emergency_event` 与 `fac_accident_incident`（后者 `is_default=false`、`event_id` 指向新建事件），并返回 B3 包络包裹的 `EmergencyEventItem`（含后端生成的真实 `id`）。端点须要求登录态。

#### Scenario: 登录态新增成功

- **WHEN** 携带有效令牌 POST 合法 `EmergencyEventCreateRequest`
- **THEN** 返回 `code=0`，`data.id` 为后端生成的真实事件 id
- **AND** `fac_accident_incident` 中存在 `event_id` = 该 id 的记录（`is_default=false`）
- **AND** 后续 `GET /api/v1/accident/rescue-incident?eventId=<该 id>` 命中该事件，而非回退默认事件

#### Scenario: 未鉴权

- **WHEN** 未携带有效令牌 POST `/api/v1/emergency-events`
- **THEN** 返回 401（B3 包络），且不写库

#### Scenario: 参数缺失

- **WHEN** POST 缺少必填字段（如 `title` / `longitude`）
- **THEN** 返回 B3 错误包络（`code!=0`），且不写库

### Requirement: 事件预警（报送）

系统须提供 `POST /api/v1/emergency-events/{id}/report`，将指定应急事件标记为已预警：在**同一事务**内将 `fac_emergency_event` 及其关联 `fac_accident_incident`（`event_id = {id}`）的 `reported` 置为 `true`（仅置该标志，不改动其他字段），并返回 B3 包络包裹的 `EmergencyEventItem`。端点须要求登录态；事件不存在时返回 404（B3 包络）。

#### Scenario: 登录态报送成功

- **WHEN** 携带有效令牌 POST `/api/v1/emergency-events/26/report`
- **THEN** 返回 `code=0`，`data.reported=true`
- **AND** `fac_emergency_event.id=26` 与对应 `fac_accident_incident` 的 `reported` 均为 `true`
- **AND** 后续 `GET /api/v1/accident/rescue-incident?eventId=26` 返回 `reported=true`（处置页据此显示「已预警」）

#### Scenario: 事件不存在

- **WHEN** 携带有效令牌 POST `/api/v1/emergency-events/<不存在 id>/report`
- **THEN** 返回 404（B3 包络），且不写库

#### Scenario: 未鉴权

- **WHEN** 未携带有效令牌 POST `/api/v1/emergency-events/{id}/report`
- **THEN** 返回 401（B3 包络），且不写库

### Requirement: 启动应急响应

系统须提供 `POST /api/v1/emergency-events/{id}/start-response`，将指定应急事件状态推进为「处置中」：在**同一事务**内把 `fac_emergency_event` 的 `status` 置为 `processing`、`status_label` 置为「处置中」，并把关联 `fac_accident_incident`（`event_id = {id}`）的 `status_name` 置为 `processing`、`map_status` 置为「处置中」（对齐 V12 种子口径：`status_name` 存枚举、`map_status` 存中文态势文案），返回 B3 包络包裹的 `EmergencyEventItem`。端点须要求登录态；事件不存在时返回 404（B3 包络）。

#### Scenario: 登录态启动成功

- **WHEN** 携带有效令牌 POST `/api/v1/emergency-events/26/start-response`
- **THEN** 返回 `code=0`，`data.status=processing`、`data.statusLabel=处置中`
- **AND** 对应 `fac_accident_incident` 的 `status_name=processing`、`map_status=处置中`
- **AND** 后续 `GET /api/v1/accident/rescue-incident?eventId=26` 返回 `status=processing`（处置页据此显示「响应已启动 / 处置中」）

#### Scenario: 事件不存在

- **WHEN** 携带有效令牌 POST `/api/v1/emergency-events/<不存在 id>/start-response`
- **THEN** 返回 404（B3 包络），且不写库

#### Scenario: 未鉴权

- **WHEN** 未携带有效令牌 POST `/api/v1/emergency-events/{id}/start-response`
- **THEN** 返回 401（B3 包络），且不写库

### Requirement: 事故救援聚合「动态快讯」按事件隔离

`GET /api/v1/accident/rescue-incident` 返回的 `dynamics`（动态快讯）须按事件隔离：演练事件返回该演练事件的专属动态、真实事件返回该事件的动态，二者各自独立、不共用同一份全局参考；事件无专属动态时回退默认事件（is_default=TRUE）动态，保证大屏不空屏。隔离键为 `fac_accident_dynamic.incident_id`（V63 迁移新增，后端内部字段，不对外暴露）。

#### Scenario: 演练事件返回演练专属动态

- **WHEN** `GET /api/v1/accident/rescue-incident?eventId=11`（储罐区消防演练，fac_accident_incident 有行且含 10 条演练专属动态）
- **THEN** `dynamics` 仅含该演练事件的 10 条演练专属动态（category 含 rescue/command/brief/awareness），不含真实事件 4 的全局动态

#### Scenario: 真实事件返回该事件动态

- **WHEN** `GET /api/v1/accident/rescue-incident?eventId=4`（默认事件，含 19 条全局参考动态）
- **THEN** `dynamics` 返回该默认事件的 19 条全局动态

#### Scenario: 无专属动态回退默认事件

- **WHEN** `GET /api/v1/accident/rescue-incident?eventId=<fac_accident_incident 无行或 dynamics 为空 的事件>`（如演练事件 12–16 未建行）
- **THEN** `dynamics` 回退返回默认事件动态（与改造前一致），大屏不空屏

#### Scenario: 调度资源/值班/辅助统计仍走全局

- **WHEN** 任意事件请求聚合数据
- **THEN** 可调度资源、值班人员、辅助统计三项仍来自全局参考主数据（fac_accident_* 的 allSorted 全量），不随事件隔离

## 约束

- 写端点 SHALL NOT 引入任何下行控制动作（零下行控制红线 `HardControlPaths` 不变）。
- 写端点（create/report/start-response）复用既有表（V12 `fac_accident_incident` / V17 `fac_emergency_event`），SHALL NOT 新增 Flyway 迁移。
- 「动态快讯按事件隔离」增强需 V63 迁移：`fac_accident_dynamic` 加 `incident_id` 列 + 索引 + 演练事件/动态种子（属新增迁移，不与上条冲突）。
