# Spec Delta: fire-alarm-disposal-persist

## 变更：`fire-alarm` capability（消防报警）

### `PUT /api/v1/fire-alarms/{alarmId}`（既有端点，扩展请求体）
- `FireAlarmUpdateRequest` 新增 4 个可选字段：
  - `handleResult: string` 处置情况文本
  - `handleTime: string` 处置时间（yyyy-MM-dd HH:mm:ss）
  - `dispatchPersonnel: string` 派单人员（逗号分隔）
  - `notifyMethod: string` 通知方式（APP/SMS 逗号分隔）
- 语义不变：字段非空才覆盖，不传不更新；成功仍返回 `FireAlarmItem`（B3 包络）+ 广播 `fire-alarm.alarm`。

### `FireAlarmItem`（响应，扩展字段）
- 新增 `handleResult` / `handleTime` / `dispatchPersonnel` / `notifyMethod`（均可空，向后兼容旧客户端）。

### 数据库
- `fac_fire_alarm` 新增 4 列（迁移 V65，三方言）。

### 权限 / 实时
- 不变：`perm=fire-alarm:ack`、`@RealtimeSync(domain="fire-alarm.alarm")`。
