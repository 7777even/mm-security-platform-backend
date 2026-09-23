## Why

生产报警（`fac_production_alarm`）是生产应急域最核心的告警对象，但 `ProductionController` 全为 GET，运行期没有任何「确认 / 处置 / 误报标记 / 派单人员 / 通知方式 / 处置情况」写回链路；前端 `AlarmDetailPanel` 的处置动作仅对 `fireAlarmId`（消防）/ `perimeterAlarmId`（周界）分支写回，生产报警走 `return true` 直接放行，等于「不落库」。这与已落地的消防报警、周界入侵告警写回能力不一致，是生产域「数据不落库」的设计缺口。用户明确要求「/security 及其他页面的告警详情要真实落库并保存状态」，生产报警是其中缺口最大、且后端此前连写端点与处置列都缺失的类型。

## What Changes

新增 `PUT /api/v1/production/alarms/{id}` 写回端点，与消防 / 周界写回对等范式：

- 权限码 `production:ack`（V74 种子，在 `fm-production` 下登记 BUTTON 级 `perm_code`，授权 ADMIN / COMMANDER / SCHEDULER / TEAM_LEADER / INNER_OPER / OUTER_OPER 六类角色），端点显式 `@RequireAuth(perm=...)`，不进 `check-endpoint-authz.mjs` 的 ALLOWLIST。
- `fac_production_alarm` 新增 6 个处置列（V73 三方言迁移）：`false_alarm` / `handle_result` / `handle_time` / `dispatch_personnel` / `notify_method` / `version`（`@Version` 乐观锁，BIGINT NOT NULL DEFAULT 0）。
- 局部更新（read-modify-write）：仅传非空字段覆盖；`status ∈ {未处置, 已确认, 处置中, 已处置}`（新增「已确认」态，区别于原 KPI 仅 3 态）、`falseAlarm ∈ {是, 否, 未核实}` 字典校验，非法 → `BusinessException(PARAM_INVALID=100)`；记录不存在 → `NOT_FOUND=404`；其余字段不传不覆盖。
- `@RealtimeSync(domain = "production.alarm")` 发布 `production.alarm.changed`，复用统一 `/ws/alarm` 总线。
- KPI「未处置告警」统计口径调整：`alarmByStatus` 中 `未处置` 与 `已确认` 合并计数（已确认 = 已确认但尚未开始处置），与详情态映射一致。
- 属 **L4**：新增 `@Version` 列 + 权限码种子 + DDL 迁移（非仅复用既有结构）。

## Impact

- 后端：新增 `ProductionAlarmUpdateRequest` DTO + `ProductionService.update` + `ProductionController` 端点；`FacProductionAlarm` 增 6 字段（含 `@Version`）；`ProductionAlarmItem` 增 5 个回填字段；V73 / V74 三方言迁移；`check-api-contract.mjs --strict` 0 漂移、`check-endpoint-authz.mjs` 写端点带 perm（仅 1 处预存 FormRecordController 无关）。
- 前端：契约 `docs/api/production.openapi.json` 增 PUT 路径 + `已确认` 状态 + `ProductionAlarmUpdateRequest` schema；`services/production.ts` 增 `updateProductionAlarm` / `ProductionAlarmUpdatePayload` / `productionAlarmChanged` / `touchProductionAlarmChanged`；`AlarmDetailPanel` 处置动作按 `productionAlarmId` 分支写回；`ProductionAlarmPanel` / `ProductionAreaView` 订阅 `production.alarm.changed` 实时刷新。
- 既有 GET 接口与表结构其余列不变；校验失败走 B3 包络（`PARAM_INVALID=100` / `NOT_FOUND=404`）。
