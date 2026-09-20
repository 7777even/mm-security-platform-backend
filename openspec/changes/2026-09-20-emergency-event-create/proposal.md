# Proposal: 应急事件新增落库（emergency-event create）

## Why
大屏应急指挥页（`/emergency`，fm-emergency 子应用）的「新增事件 / 新增演练 / 新增极端天气」
此前仅写入前端内存 + sessionStorage 草稿，后端无记录，导致两个缺陷：

1. 新增后刷新即丢失（后端无行可拉）；
2. 点「去处置」时 `AccidentRescueService.incident(eventId)` 按 `event_id` 查不到新事件，
   回退 `is_default=TRUE` 的默认事件 —— 表现为「总跳到乙烯裂解装置区火灾（eventId=17）」。

需补一个写入端点，让新增事件真正落库，并同事务写入处置页所需的事故救援事件表。

## What Changes
- 契约真源**扩展** `docs/api/emergency-event.openapi.json`：`/emergency-events` 新增 `POST`
  （operationId `createEmergencyEvent`）+ 新 schema `EmergencyEventCreateRequest`（与后端 DTO 同名，19 字段）。
- 后端新增 `dto.EmergencyEventCreateRequest`、`EmergencyEventController.create`（`@RequireAuth` 仅登录态）、
  `EmergencyEventService.create`（`@Transactional` 同事务写 `fac_emergency_event` + `fac_accident_incident`）。
- **复用既有表与迁移**（V12 `fac_accident_incident` / V17 `fac_emergency_event`），无新增 Flyway 迁移、无权限模型变更。
- 前端 `services/emergencyEvent.ts` 增 `createEmergencyEvent`；`useFireEmergencyEventList` 改 POST-first 拿真实 id，
  后端不可达时回落 sessionStorage 草稿（弱网兜底）。

## Capabilities
- 应急事件由「只读参考数据」扩展为「只读 + 登录态新增」。新增事件同事务落到事故救援事件表
  （`is_default=false`、`event_id` 指向新事件），使「去处置」可按 `event_id` 精确定位，
  不再回退默认事件。

## Impact
- **L3 变更**：复用既有表加写端点，无新迁移、无权限模型变更（仅 `@RequireAuth` 登录态，非 ADMIN）。
- 策略：**后端为主 + 失败回退本地草稿** —— 后端可达即落库；不可达时前端 sessionStorage 兜底展示。
- 达梦 / PostgreSQL 无结构性变更（无新迁移），沿用现状「静态同步维护」。
- 写端点不引入任何下行控制动作，零下行控制红线 `HardControlPaths` 不变。
