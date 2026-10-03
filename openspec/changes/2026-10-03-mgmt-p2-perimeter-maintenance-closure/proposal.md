# Proposal: 周界告警 LIST + 维保子写端点（后端实现）

## Why

P2 批次 1（前端契约四同步 `2026-10-03-mgmt-p2-security-crud`）补齐了安防域写端点契约，但复核暴露两处能力缺口：

1. **周界入侵告警缺 LIST GET**：前端管理端台账需要全量列表，但后端只有 `latest` / `{id}` / 写回 / 删除，
   缺一个「按告警时间倒序返回全部」的列表端点，导致管理端只能调 `latest` 看一条。新增
   `GET /api/v1/security/perimeter-alarms`（`SecurityService.listPerimeterAlarms`）。
2. **消防设施维保缺子写端点**：`fac_fire_facility_maintenance` 维保记录只能随台账整体返回，无法单独新增/删除。
   新增 `POST /api/v1/fire-facility/ledger/{ledgerId}/maintenance` 与
   `DELETE /api/v1/fire-facility/maintenance/{recordId}`。

权限码 `security:perimeter-create` / `security:perimeter-ack` / `security:perimeter-delete` /
`fire-facility:ledger:write` 已由 P2 批次 1 的 Flyway 播种（V70/V76/V99），本次仅消费，无需新增播种。

## What Changes

### 周界告警 LIST

- `SecurityService.listPerimeterAlarms()`：按 `alarm_time desc, id desc` 拉全量 `fac_perimeter_alarm`，
  map 为 `PerimeterAlarmDetail`（复用 `toPerimeterAlarmDetail`）。
- `SecurityController`：新增 `GET /api/v1/security/perimeter-alarms`，返回 `List<PerimeterAlarmDetail>`。
- 修复 `SecurityService.java` 缺 `java.util.stream.Collectors` 导入（编译期符号缺失）。

### 维保子写端点

- `FireFacilityMaintenanceRecord` DTO：增加 `id` / `ledgerId` 字段（删除引用 + 回显）。
- 新增 `FireFacilityMaintenanceWriteRequest` DTO（`date` / `content` 必填，`reportFile` 可空）。
- `FireFacilityService`：
  - `toMaintenanceRecord(FacFireFacilityMaintenance)`：map `id` / `ledgerId`。
  - `createMaintenance(ledgerId, req)`：校验 ledger 存在（`NOT_FOUND` 否则）、`date`/`content` 非空；
    `sort_no` 按该台账内 `max+1`（对齐 LedgerIdSupport 显式 max+1 范式，规避 H2/PG/DM 序列滞后撞主键）；
    插入后返回 `toMaintenanceRecord(e)`；`@RealtimeSync(domain="fire-facility.ledger")`。
  - `deleteMaintenance(recordId)`：记录不存在返回 `NOT_FOUND`，否则物理删除；`@RealtimeSync(domain="fire-facility.ledger")`。
- `FireFacilityController`：新增 `POST /api/v1/fire-facility/ledger/{ledgerId}/maintenance`
  （`@RequireAuth(perm="fire-facility:ledger:write")`）与 `DELETE /api/v1/fire-facility/maintenance/{recordId}`。

### 单测

- `SecurityControllerTest.listPerimeterAlarms_returnsAllAlarmsOrdered`：验证按时间倒序返回 2 条。
- `FireFacilityControllerTest.createMaintenance_returnsCreatedRecordWithLedgerBinding` /
  `deleteMaintenance_returnsOkEnvelopeAndDelegatesToService`：验证路径与 service 委托。

## Capabilities

- `security`（后端实现）：新增「周界入侵告警 LIST」端点（与既有 POST/PUT/DELETE 构成完整 CRUD）。
- `fire-facility`（后端实现）：新增「消防设施维保子写」端点（`createMaintenance` / `deleteMaintenance`），
  经 `fire-facility.ledger` 广播。

## Impact

- 影响：`SecurityService.java`、`SecurityController.java`、`FireFacilityMaintenanceRecord.java`（新）、
  `FireFacilityMaintenanceWriteRequest.java`（新）、`FireFacilityService.java`、`FireFacilityController.java`、
  及两个 Controller 单测。
- 风险：无 Flyway 迁移（不建新表，复用既有 `fac_facility_maintenance`），无序列/版本号改动。
- 守门：`scripts/check-api-contract.mjs --strict` schema 漂移 0；新增 3 条路由（含 perimeter 既有 5 条）全部落入「已对齐」。
  存量 12 路由差异为 gate/bollard/patrol 的 `{id}` 路径形式技术债，非本次引入。
