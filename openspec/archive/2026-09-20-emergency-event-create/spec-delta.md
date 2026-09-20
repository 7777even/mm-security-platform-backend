# Spec Delta: emergency-event（应急事件写端点）

## ADDED

### Requirement: 新增应急事件

系统 SHALL 提供 `POST /api/v1/emergency-events`，接受 `EmergencyEventCreateRequest`，
在**同一事务**内写入 `fac_emergency_event` 与 `fac_accident_incident`（后者 `is_default=false`、
`event_id` 指向新建事件），并返回 B3 包络包裹的 `EmergencyEventItem`（含后端生成的真实 `id`）。
端点 SHALL 要求登录态。

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

## 约束

- 写端点 SHALL NOT 引入任何下行控制动作（零下行控制红线不变）。
- `scene` 由前端传入（`FIRE` / `PRELIMINARY`）；缺省 `FIRE`。
- 复用既有表，SHALL NOT 新增 Flyway 迁移。
