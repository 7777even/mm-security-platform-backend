# Spec Delta: perimeter-alarm-writeback

## Capability: security-perimeter（周界入侵告警）

### ADDED — 周界入侵告警处置写回

- 系统 SHALL 提供 `PUT /api/v1/security/perimeter-alarms/{id}`，对单条周界入侵告警做处置状态流转或误报标记，与消防报警写回对等（L4）。
- 请求体 SHALL 为局部更新（read-modify-write）：仅非空字段写入，未传字段保持不变。
  - `status` ∈ {`未确认`, `已确认`, `已派单`, `已处理`}（对齐字典 `perimeter_alarm_status`）。
  - `falseAlarm` ∈ {`是`, `否`, `未核实`}（对齐字典 `perimeter_alarm_false`）。
  - `handleResult` / `handleTime` / `dispatchPersonnel` / `notifyApp` / `notifySms` 为自由文本/布尔字段，不校验、不传不覆盖。
- 端点 SHALL 要求权限码 `security:perimeter-ack`（V70 种子，BUTTON 级，授权 ADMIN / COMMANDER / SCHEDULER / TEAM_LEADER / INNER_OPER / OUTER_OPER 六类角色）；未授权返回 B3 `code=403`。
- 当 `id` 不存在时，SHALL 返回 B3 `code=404`（不抛 500）。
- 当枚举非法时，SHALL 返回 B3 `code=100`（参数非法）。
- 成功时 SHALL 落 `fac_perimeter_alarm`（V69 新增 `@Version` 乐观锁列防护并发覆盖）并返回 `PerimeterAlarmDetail`（B3 包络），同时广播实时域 `security.perimeter-alarm.changed`，复用统一 `/ws/alarm` 总线。

#### Scenario: 确认周界告警

- **GIVEN** 一条 `status=未确认` 的周界告警 `PA-20260923-001`
- **WHEN** 以 `{"status":"已确认"}` 请求 `PUT /security/perimeter-alarms/PA-20260923-001`
- **THEN** 该行 `status` 变为 `已确认`，响应 `code=0` 且 `data.status=已确认`，并广播 `security.perimeter-alarm.changed`

#### Scenario: 标记误报

- **GIVEN** 一条周界告警记录
- **WHEN** 以 `{"falseAlarm":"是"}` 请求写回
- **THEN** 仅 `falseAlarm` 变为 `是`，其余字段保持原值

#### Scenario: 告警不存在

- **WHEN** 请求 `PUT /security/perimeter-alarms/UNKNOWN`
- **THEN** 返回 B3 `code=404`，不写库

#### Scenario: 非法枚举

- **WHEN** 请求 `PUT /security/perimeter-alarms/PA-...`，`status` 为非枚举值
- **THEN** 返回 B3 `code=100`，不写库
