# perimeter-alarm Specification

## Purpose

周界入侵告警（前端「治安报警」）的读写能力。覆盖最新告警查询、详情/快照读取、处置写回（PUT）与手工创建（POST）。落库表 `fac_perimeter_alarm`，状态变更经 `@RealtimeSync(domain="security.perimeter-alarm")` 广播 `<domain>.changed`（即 `security.perimeter-alarm.changed`），驱动前端 `SecurityStatusPanel` / `SecurityAlarmDetailDialog` 自动刷新。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/security/perimeter-alarms/latest` | 登录即可 | 取最新一条周界告警详情 |
| GET | `/api/v1/security/perimeter-alarms/{id}` | 登录即可 | 按 id 取单条详情 |
| GET | `/api/v1/security/perimeter-alarms/{id}/snapshot` | 登录即可 | 取告警关联快照资源 |
| PUT | `/api/v1/security/perimeter-alarms/{id}` | `security:perimeter-ack` | 处置写回（状态/误报/处置说明等） |
| POST | `/api/v1/security/perimeter-alarms` | `security:perimeter-create` | 手工录入一条周界入侵告警 |

所有响应统一 B3 包络（HTTP 200 + `code=0` 为成功；参数/业务失败返回 HTTP 200 但 `code!=0`，**不返回 400**）。未鉴权返回 401，无权限返回 403。

## Requirements

### Requirement: 周界告警查询

系统 SHALL 提供最新告警与单条详情查询，返回 `PerimeterAlarmDetail`（前端契约 `PerimeterAlarmDetail` 与 `GET /security/perimeter-alarms` 对齐）。

#### Scenario: 查询最新告警

- **WHEN** 已鉴权用户调用 `GET /api/v1/security/perimeter-alarms/latest`
- **THEN** 返回最新一条 `PerimeterAlarmDetail`（无记录时 B3 包络 `data=null`）

#### Scenario: 查询单条详情

- **WHEN** 已鉴权用户调用 `GET /api/v1/security/perimeter-alarms/{id}`
- **THEN** 返回该条 `PerimeterAlarmDetail`；目标不存在时 B3 包络 `data=null`

### Requirement: 周界告警处置写回

系统 SHALL 提供 `PUT /api/v1/security/perimeter-alarms/{id}` 端点，允许持有 `security:perimeter-ack` 的操作员更新处置状态/误报标记/处置说明等字段，并以 `@RealtimeSync` 广播刷新。

#### Scenario: 处置写回成功

- **WHEN** 已鉴权且持有 `security:perimeter-ack` 的操作员提交处置字段
- **THEN** 返回更新后的 `PerimeterAlarmDetail`（B3 包络 `code=0`）并广播 `security.perimeter-alarm.changed`

#### Scenario: 无处置权限

- **WHEN** 已鉴权但不持有 `security:perimeter-ack`
- **THEN** 返回 403

### Requirement: 创建周界入侵告警

系统 SHALL 提供 `POST /api/v1/security/perimeter-alarms` 端点，允许持有 `security:perimeter-create` 权限的操作员手工录入一条周界入侵告警，落库 `fac_perimeter_alarm` 并经 `@RealtimeSync(domain="security.perimeter-alarm")` 广播，使订阅面板自动刷新。

#### Scenario: 操作员录入新告警（成功）

- **WHEN** 已鉴权且持有 `security:perimeter-create` 的操作员提交 `title` 等字段
- **THEN** 系统生成 `alarmCode`（格式 `PA-yyyyMMdd-HHmmss`）、置 `status='未确认'`、`falseAlarm='未核实'`、`source='人工录入'`、`version=0`，返回 `PerimeterAlarmDetail`（HTTP 200，B3 包络 `code=0`）
- **AND** 广播 `security.perimeter-alarm.changed`，前端 `SecurityStatusPanel` 重拉最新告警

#### Scenario: 缺标题被拒（B3 包络）

- **WHEN** 提交请求 `title` 为空
- **THEN** 返回 HTTP 200 且 B3 包络 `code!=0`（非 400），提示标题必填

#### Scenario: 未鉴权

- **WHEN** 请求未携带 token
- **THEN** 返回 401

#### Scenario: 无创建权限

- **WHEN** 已鉴权但不持有 `security:perimeter-create`
- **THEN** 返回 403
