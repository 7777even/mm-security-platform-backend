# Tasks: 消防设施报警处置端点（后端）

- [x] `FireFacilityService` 抽出 `private doUpdateFault(FacFireFacilityFault e, req)`，收敛写回核心逻辑
- [x] `FireFacilityService.updateFault` 保留 `@RealtimeSync`，按主键定位后委托 `doUpdateFault`
- [x] `FireFacilityService.updateAlarm` 新增：报警 id → 数字串 → SQL 反查故障 → 委托 `doUpdateFault`
      （空数字串 `PARAM_INVALID` / 未命中 `NOT_FOUND`）
- [x] `FireFacilityController` 新增 `PUT /alarms/{alarmId}`，`@RequireAuth(perm="fire-facility:handle")`
- [x] `FireFacilityServiceTest` 补 3 例：反查落库并追加时间线 / 未命中 NOT_FOUND / 非法 id PARAM_INVALID
- [x] `check-api-contract.mjs --strict` 通过（路由差异 0 / schema 漂移 0）
- [x] 全量 `mvn test` 通过（service 改动，不使用定向测试冒充）
