# emergency-event Specification

## Purpose

大屏应急指挥「应急事件」域数据源（`fac_emergency_event`，配 `fac_accident_incident`）：事件分组列表（只读）、登录态新增与登录态事件预警（报送）。新增写端点由 Change `2026-09-20-emergency-event-create` 回填；事件预警端点由 Change `2026-09-21-emergency-event-report` 回填；列表端点沿用既有 `GET /emergency-events`。

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

## 约束

- 写端点 SHALL NOT 引入任何下行控制动作（零下行控制红线 `HardControlPaths` 不变）。
- 复用既有表（V12 `fac_accident_incident` / V17 `fac_emergency_event`），SHALL NOT 新增 Flyway 迁移。
