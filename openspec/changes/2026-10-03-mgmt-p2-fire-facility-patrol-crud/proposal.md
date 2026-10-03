# Proposal: 消防设施台账与防火巡查 CRUD（P2 批次 3）

## 问题

P2 设施与安防域中，**消防设施台账**（`fac_fire_facility_ledger`）与**防火巡查记录**（`fac_fire_patrol`）此前只有只读列表：

1. 两类台账无新增/编辑/删除入口，设备信息与巡查台账变更只能改库。
2. 写请求若接受设备实时状态 / 检查项结果字段会突破零下行控制红线（巡查项结果由 `fac_fire_patrol_item_result` 单独维护，本轮只读）。
3. `fac_fire_patrol` 缺版本列，并发编辑会静默后写覆盖。

## 目标

扩展 `FireFacilityService` / `FireMonitoringService`，把消防设施台账与防火巡查记录做成**全套 CRUD**，并严守零下行控制：

- 消防设施台账：`POST /fire-facility/ledger`、`PUT /fire-facility/ledger/{id}`、`DELETE /fire-facility/ledger/{id}`，权限码 `fire-facility:ledger:write`，`@RealtimeSync(domain="fire-facility.ledger")`。
- 防火巡查记录：`POST /fire/patrols`、`PUT /fire/patrols/{id}`、`DELETE /fire/patrols/{id}`，权限码 `fire:patrol-write`，`@RealtimeSync(domain="fire.patrol-record")`。
- 写请求 DTO：`FireFacilityLedgerWriteRequest`（设施编码/名称/类型必填；location/device/maintainerName/maintainerPhone/enabled 可选）、`FirePatrolWriteRequest`（巡查日期必填；shift/dutyPerson/patrolCount/locations/completed/workOrderNo 可选）。**两个 DTO 均不含 `status` / 检查项结果字段**（零下行控制）。
- V99 三方言迁移：在 `fm-fire` 父菜单下登记按钮级菜单 `fm-fire-ledger-write`（perm `fire-facility:ledger:write`）并授权 ADMIN 及岗位角色。
- V102 三方言迁移：登记 `fm-fire-patrol-record-write`（perm `fire:patrol-write`）并授权 ADMIN 及岗位角色；给 `fac_fire_patrol` 增加 `version BIGINT DEFAULT 0` 乐观锁列。
- 契约四同步：前端 `docs/api/fire-facility.openapi.json` / `docs/api/fire-monitoring.openapi.json` 为唯一真源；`check-api-contract.mjs --strict` 守门（schema 漂移 0）。

## 非目标

- 不为设施设备台账接入设备实时状态下发（零下行控制，`status` 仅读）。
- 不改动巡查项结果（`checkItems` / `fac_fire_patrol_item_result`），本轮只读。
- 不新增维保工单写端点（无对应后端实现，留待后续批次）。

## 影响面

- 新增 `dto/FireFacilityLedgerWriteRequest.java`、`dto/FirePatrolWriteRequest.java`。
- `FacFirePatrol` 实体加 `@Version` 字段；`FacFireFacilityLedger` 主键沿用 `LedgerIdSupport` 显式 max+1 分配（规避 H2/PG/达梦自增序列滞后撞主键）。
- `FireFacilityService` 新增 `createLedger`/`updateLedger`/`deleteLedger`，标注 `@RealtimeSync`。
- `FireMonitoringService` 新增 `createFirePatrol`/`updateFirePatrol`/`deleteFirePatrol`，标注 `@RealtimeSync`。
- `FireFacilityController` / `FireMonitoringController` 各新增 3 个写端点，全部 `@RequireAuth(perm=...)` + `@Valid`。
- 三方言迁移 `V99__fire_facility_ledger_crud_perm.sql` / `V102__fire_patrol_crud_perm.sql`（h2 / postgresql / dameng 同名同号）。
- 回退：移除 6 个写端点与 V99/V102 种子、回退 `version` 列即可；前端恢复为只读台账。

## Capabilities

- `fire-facility`（消防设施）：新增「消防设施台账」全量 CRUD 能力，新增 `fire-facility.ledger` 实时广播域。
- `fire-monitoring`（消防监控）：新增「防火巡查记录」全量 CRUD 能力，新增 `fire.patrol-record` 实时广播域。
