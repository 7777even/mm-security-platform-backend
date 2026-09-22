# Tasks: fire-alarm-disposal-persist（后端）

- [x] 新增迁移 `V65__fire_alarm_disposal_fields.sql`（h2/postgresql/dameng），为 `fac_fire_alarm` 加 4 列。
- [x] `FacFireAlarm` 实体加 `handleResult/handleTime/dispatchPersonnel/notifyMethod` 字段。
- [x] `FireAlarmUpdateRequest` 加 4 个可选字段（自由文本，无枚举约束）。
- [x] `FireAlarmItem` 加 4 字段（回显）+ `FireAlarmService.toItem` 回填。
- [x] `FireAlarmService.update`：4 字段非空时 `set`（read-modify-write + @Version 乐观锁）。
- [x] 同步前端契约 `docs/api/fire-alarm.openapi.json`（`FireAlarmUpdateRequest` / `FireAlarmItem` 各加 4 字段）。
- [x] `FireAlarmServiceTest` 补 4 字段读写用例；跑 `check-api-contract.mjs --strict`（漂移 0）+ `check-endpoint-authz.mjs`（0 违规）。
- [ ] 推送后：真实 8787 实例 curl `PUT /fire-alarms/FA-...` 带 4 字段，回读 `GET /fire-alarms` 验证已落库。
