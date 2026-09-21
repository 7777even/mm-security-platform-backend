# Tasks

- [x] 后端 `EmergencyEventService.report` 同事务置两表 `reported=true`（事件不存在 404）
- [x] 后端 `EmergencyEventController` `POST /emergency-events/{id}/report`（`@RequireAuth`）
- [x] 前端契约 `docs/api/emergency-event.openapi.json` 增加路径（四同步真源）
- [x] `scripts/check-endpoint-authz.mjs` ALLOWLIST 登记 `EmergencyEvent#report`
- [x] 前端 `reportEmergencyEvent` + `IncidentDetailPanel` emit('report') + 「已预警」状态徽标
- [x] 前端 `AccidentRescuePayload.status` / `AccidentRescueMap` 联合类型加入 `warning`
- [x] `node scripts/check-api-contract.mjs --strict` 0 差异；`vue-tsc` / `eslint` 0 error
- [x] `EmergencyEventServiceTest` 增加 report 单测（标记 + 404）
