# Spec Delta: add-fire-alarm-writeback

## Capability: fire-alarm（消防报警）

### ADDED — 报警处置写回

- 系统 SHALL 提供 `PUT /api/v1/fire-alarms/{alarmId}`，对单条消防报警做处置状态流转或误报标记。
- 请求体 SHALL 为局部更新：仅非空字段被写入，未传字段保持不变。
  - `status` ∈ {`ACTIVE`, `ACKED`, `DISPATCHED`, `CLOSED`}（对齐字典 `fire_alarm_status`）。
  - `falseAlarm` ∈ {`是`, `否`, `未核实`}（对齐字典 `fire_alarm_false`）。
- 端点 SHALL 要求权限码 `fire-alarm:ack`；未授权返回 B3 `code=403`。
- 当 `alarmId` 不存在时，SHALL 返回 B3 `code=404`（不抛 500）。
- 当枚举非法时，SHALL 返回 B3 `code=100`（参数非法）。
- 成功时 SHALL 落 `fac_fire_alarm` 并返回更新后的 `FireAlarmItem`（B3 包络），同时广播实时域 `fire-alarm.alarm`。
- 更新 SHALL 使用实体 `@Version` 乐观锁，避免并发覆盖。

#### Scenario: 确认报警
- **GIVEN** 一条 `status=ACTIVE` 的报警 `FA-20260907-001`
- **WHEN** 以 `{"status":"ACKED"}` 请求 `PUT /fire-alarms/FA-20260907-001`
- **THEN** 该行 `status` 变为 `ACKED`，响应 `code=0` 且 `data.status=ACKED`，并广播 `fire-alarm.alarm.changed`

#### Scenario: 标记误报
- **GIVEN** 一条报警记录
- **WHEN** 以 `{"falseAlarm":"是"}` 请求写回
- **THEN** 仅 `falseAlarm` 变为 `是`，`status` 保持不变

#### Scenario: 报警不存在
- **WHEN** 请求 `PUT /fire-alarms/UNKNOWN`
- **THEN** 返回 B3 `code=404`，不写库

#### Scenario: 非法枚举
- **WHEN** 请求 `PUT /fire-alarms/FA-...`，`status` 为非枚举值
- **THEN** 返回 B3 `code=100`，不写库
