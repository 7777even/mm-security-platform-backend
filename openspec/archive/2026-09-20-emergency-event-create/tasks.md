# Tasks

- [x] 契约真源扩展 `docs/api/emergency-event.openapi.json`（`POST /emergency-events` + `EmergencyEventCreateRequest`）
- [x] 后端 `dto.EmergencyEventCreateRequest`（类名/字段对齐契约 schema）
- [x] `EmergencyEventController.create`（`@PostMapping` + `@RequireAuth` + `@Valid`）
- [x] `EmergencyEventService.create`（`@Transactional` 同事务写 `fac_emergency_event` + `fac_accident_incident`，`is_default=false`）
- [x] `EmergencyEventControllerTest` 补 POST 用例（成功 + 校验失败；5 例全绿）
- [x] `check-api-contract.mjs --strict` 通过（路由 0 差异 / schema 0 漂移）
- [x] 回写 `docs/system-facts.md`
- [x] 实链路冒烟（`SERVER__PORT=8899`：401 负例 + 新建事件按 `event_id` 命中）
- [x] 按 scope 提交推送后端实现
- [x] 归档（回填 `openspec/specs/emergency-event/spec.md` 并 `git mv` 到 `openspec/archive/2026-09-20-emergency-event-create`）
