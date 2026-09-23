# Spec Delta: perimeter-alarm-create

## Capability: perimeter-alarm

> 周界入侵告警（前端「治安报警」）的读写能力。既有 capability 仅覆盖查询与处置写回（PUT）；本变更补齐「手工创建」。

### ADDED

#### Requirement: 创建周界入侵告警
系统 SHALL 提供 `POST /api/v1/security/perimeter-alarms` 端点，允许持有 `security:perimeter-create` 权限的操作员手工录入一条周界入侵告警，落库 `fac_perimeter_alarm` 并经 `@RealtimeSync(domain="security.perimeter-alarm")` 广播，使订阅面板自动刷新。

##### Scenario: 操作员录入新告警（成功）
- **WHEN** 已鉴权且持有 `security:perimeter-create` 的操作员提交 `title` 等字段
- **THEN** 系统生成 `alarmCode`（格式 `PA-yyyyMMdd-HHmmss`）、置 `status='未确认'`、`falseAlarm='未核实'`、`source='人工录入'`、`version=0`，返回 `PerimeterAlarmDetail`（HTTP 200，B3 包络 `code=0`）
- **AND** 广播 `security.perimeter-alarm.changed`，前端 `SecurityStatusPanel` 重拉最新告警

##### Scenario: 缺标题被拒（B3 包络）
- **WHEN** 提交请求 `title` 为空
- **THEN** 返回 HTTP 200 且 B3 包络 `code!=0`（非 400），提示标题必填

##### Scenario: 未鉴权
- **WHEN** 请求未携带 token
- **THEN** 返回 401

##### Scenario: 无创建权限
- **WHEN** 已鉴权但不持有 `security:perimeter-create`
- **THEN** 返回 403
