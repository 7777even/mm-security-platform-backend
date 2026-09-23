## Why

周界入侵告警（`fac_perimeter_alarm`）是治安防控域最贴近消防报警的对象——`status` / `falseAlarm` / `handleResult` / `handleTime` / `dispatchPersonnel` / `notifyApp` / `notifySms` 字段形状与 `fac_fire_alarm` 几乎一致，但 `SecurityController` 全为 GET，运行期没有任何确认 / 派单 / 处置写回链路。这与已经落地的消防报警「写回 + 实时」能力不一致，是治安防控域「数据不落库」的设计缺口。

## What Changes

新增 `PUT /api/v1/security/perimeter-alarms/{id}` 写回端点，与消防报警写回对等范式：

- 权限码 `security:perimeter-ack`（V70 种子，在 `fm-security` 下登记 BUTTON 级 `perm_code`，授权 ADMIN / COMMANDER / SCHEDULER / TEAM_LEADER / INNER_OPER / OUTER_OPER 六类角色），端点显式 `@RequireAuth(perm=...)`，不进 `check-endpoint-authz.mjs` 的 ALLOWLIST。
- `fac_perimeter_alarm` 新增 `@Version` 乐观锁列（V69 三方言迁移），防护并发确认 / 处置。
- 局部更新（read-modify-write）：仅传非空字段覆盖；`status ∈ {未确认,已确认,已派单,已处理}`、`falseAlarm ∈ {是,否,未核实}` 字典校验，非法 → `BusinessException(PARAM_INVALID=100)`；记录不存在 → `NOT_FOUND`；其余字段不传则不覆盖。
- `@RealtimeSync(domain = "security.perimeter-alarm")` 发布 `security.perimeter-alarm.changed`，复用统一 `/ws/alarm` 总线。
- 属 **L4**：新增 `@Version` 列 + 权限码种子 + DDL 迁移（非仅复用既有结构）。

## Impact

- 后端：新增 `PerimeterAlarmUpdateRequest` DTO + `SecurityService.updatePerimeterAlarm` + `SecurityController` 端点；V69 / V70 三方言迁移；`SecurityPerimeterAlarmWriteBackTest`（纯 Mockito 6 例）绿。
- 前端：契约 `docs/api/security.openapi.json` 增 PUT 路径 + `PerimeterAlarmUpdateRequest` schema；`services/security.ts` 增 `updatePerimeterAlarm` / `PerimeterAlarmUpdatePayload` / `perimeterAlarmChanged` / `touchPerimeterAlarmChanged`；`AlarmDetailPanel` 处置动作按 `perimeterAlarmId` 分支写回；`SecurityStatusPanel` 订阅 `security.perimeter-alarm.changed` 实时刷新。
- 既有 GET 接口与表结构其余列不变；校验失败走 B3 包络（`PARAM_INVALID=100` / `NOT_FOUND=404`）。
