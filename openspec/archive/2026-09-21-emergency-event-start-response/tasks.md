# Tasks

- [x] 后端 `EmergencyEventService.startResponse` 同事务置两表状态为 processing/处置中（不存在 404）
- [x] 后端 `EmergencyEventController` `POST /emergency-events/{id}/start-response`（`@RequireAuth`）
- [x] 前端契约 `docs/api/emergency-event.openapi.json` 增加路径（四同步真源）
- [x] `scripts/check-endpoint-authz.mjs` ALLOWLIST 登记 `EmergencyEvent#startResponse`
- [x] 前端 `startEmergencyResponse` + `handleStartEmergencyResponse` 调接口/刷新 + 演练守卫
- [x] `responseStarted` 改 computed（含 `incident.status` processing/处置中）→ 刷新后仍「响应已启动」
- [x] `node scripts/check-api-contract.mjs --strict` 0 差异；`vue-tsc` / `eslint` 0 error
- [x] `EmergencyEventServiceTest` 增加 startResponse 单测（推进 + 404）
