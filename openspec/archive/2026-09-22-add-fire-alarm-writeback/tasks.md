# Tasks: add-fire-alarm-writeback（后端）

- [x] [TDD] 先写失败测试：`FireAlarmService.update` 断言合法枚举落库、非法枚举抛 `BusinessException(100)`、
      不存在抛 `BusinessException(404)`（standalone Mockito + 纯内存态）。
- [x] 新增 `dto/FireAlarmUpdateRequest`（status/falseAlarm 均可空，局部更新）。
- [x] `FireAlarmService.update`：`selectById` → 校验枚举 → 覆盖非空字段 → `updateById`（@Version 自动并发防护）→ 回 `FireAlarmItem`。
- [x] `FireAlarmController` 新增 `PUT /fire-alarms/{alarmId}`，`@RequireAuth(perm="fire-alarm:ack")`。
- [x] 写方法加 `@RealtimeSync(domain="fire-alarm.alarm")`。
- [x] 同步 `frontend-scaffold/docs/api/fire-alarm.openapi.json`（新增 path + `FireAlarmUpdateRequest` + 修正 status 描述）。
- [x] 跑 `scripts/check-api-contract.mjs`（路由+schema 双层级，期望漂移 0）与 `check-endpoint-authz.mjs`。
- [x] 推送后：真实 8787 实例 curl 对拍 `PUT /fire-alarms/FA-...` 后回读 `GET /fire-alarms` 验证 status 已变更（已验证：status/falseAlarm 落库）。
