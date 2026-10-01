# Spec Delta: fire-alarm-crud

## Capability: fire-alarm（消防报警）

### ADDED — 报警新增
- 系统 SHALL 提供 `POST /api/v1/fire-alarms`，创建一条消防报警。
- 请求体 SHALL 含必填 `title`（报警名称）、`time`（报警时间，yyyy-MM-dd HH:mm:ss）；其余字段可选。
- 当 `title` / `time` 为空时，SHALL 返回 B3 `code=100`（参数非法）。
- 当 `status` 传入但非 `{ACTIVE,ACKED,DISPATCHED,CLOSED}` 时，SHALL 返回 B3 `code=100`。
- 成功时 SHALL 落 `fac_fire_alarm`，返回创建后的 `FireAlarmItem`（B3 包络，`alarmId` 非空），
  并广播实时域 `fire-alarm.alarm`。

### ADDED — 报警删除（真删除）
- 系统 SHALL 提供 `DELETE /api/v1/fire-alarms/{alarmId}`，真删除该条报警。
- 端点 SHALL 要求权限码 `fire-alarm:delete`；未授权返回 B3 `code=403`。
- 当 `alarmId` 不存在时，SHALL 返回 B3 `code=404`，不写库。
- 成功时 SHALL 广播实时域 `fire-alarm.alarm`。

### MODIFIED — 报警全字段更新（PUT 扩字段）
- `PUT /api/v1/fire-alarms/{alarmId}` 的请求体 SHALL 扩为全字段局部更新（19 字段，原 2 字段）
  `FireAlarmUpdateRequest`：仅非空字段被写入，未传字段保持不变。
- 端点权限码保持 `fire-alarm:ack` 不变。
- 当枚举非法时 SHALL 返回 B3 `code=100`；当 `alarmId` 不存在时 SHALL 返回 B3 `code=404`。
- 更新 SHALL 使用实体 `@Version` 乐观锁。

#### Scenario: 新增一条报警
- **GIVEN** 角色持 `fire-alarm:create` 权限
- **WHEN** 以 `{"title":"联动测试报警","time":"2026-10-01 21:00:00","typeLabel":"火灾报警","status":"ACTIVE"}` 请求 `POST /fire-alarms`
- **THEN** 响应 `code=0` 且 `data.alarmId` 非空（形如 `FA-...`），并广播 `fire-alarm.alarm.changed`

#### Scenario: 缺必填字段
- **WHEN** 以 `{"time":"2026-10-01 21:00:00"}`（无 title）请求 `POST /fire-alarms`
- **THEN** 返回 B3 `code=100`，不写库

#### Scenario: 删除不存在的报警
- **WHEN** 请求 `DELETE /fire-alarms/UNKNOWN`
- **THEN** 返回 B3 `code=404`，不写库

#### Scenario: 全字段编辑
- **GIVEN** 一条 `alarmId=FA-x` 的报警
- **WHEN** 以全 19 字段非空请求 `PUT /fire-alarms/FA-x`
- **THEN** 该行所有字段被覆盖为传入值，响应 `code=0` 且 `data` 逐字段一致
