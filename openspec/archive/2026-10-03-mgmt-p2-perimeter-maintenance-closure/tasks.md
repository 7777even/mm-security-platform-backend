# Tasks: 周界告警 LIST + 维保子写端点（后端实现）

## 周界告警 LIST（Task 1）

- [x] `SecurityService.listPerimeterAlarms()`：按 `alarm_time desc, id desc` 拉全量 `fac_perimeter_alarm`，map `toPerimeterAlarmDetail`
- [x] `SecurityController`：新增 `GET /api/v1/security/perimeter-alarms`，返回 `List<PerimeterAlarmDetail>`
- [x] `SecurityService.java` 补 `import java.util.stream.Collectors`

## 维保子写端点（Task 2）

- [x] `FireFacilityMaintenanceRecord` DTO 增加 `id` / `ledgerId`
- [x] 新增 `FireFacilityMaintenanceWriteRequest` DTO（`date`/`content` 必填，`reportFile` 可空）
- [x] `FireFacilityService.toMaintenanceRecord` map `id`/`ledgerId`
- [x] `FireFacilityService.createMaintenance(ledgerId, req)`：校验 ledger 存在、`date`/`content` 非空、`sort_no=max+1`、`@RealtimeSync("fire-facility.ledger")`
- [x] `FireFacilityService.deleteMaintenance(recordId)`：`NOT_FOUND` 或物理删除、`@RealtimeSync("fire-facility.ledger")`
- [x] `FireFacilityController`：新增 `POST /api/v1/fire-facility/ledger/{ledgerId}/maintenance`（`@RequireAuth(perm="fire-facility:ledger:write")`）与 `DELETE /api/v1/fire-facility/maintenance/{recordId}`

## 单测（Task 3）

- [x] `SecurityControllerTest.listPerimeterAlarms_returnsAllAlarmsOrdered`：验证时间倒序返回 2 条
- [x] `FireFacilityControllerTest.createMaintenance_returnsCreatedRecordWithLedgerBinding`：验证 id=5/ledgerId=1/content 落库绑定
- [x] `FireFacilityControllerTest.deleteMaintenance_returnsOkEnvelopeAndDelegatesToService`：验证委托 `service.deleteMaintenance(9L)`

## 守门（Task 4）

- [x] 后端 `mvn -o test -Dtest=SecurityControllerTest,FireFacilityControllerTest -DfailIfNoTests=false -Djacoco.skip=true`：BUILD SUCCESS（41 tests, 0 failures）
- [x] `node scripts/check-api-contract.mjs --strict`：schema 漂移 0；新增路由全部已对齐

## 收尾（Task 5）

- [x] 双仓（前端 `feature/scaffold-rebuild` / 后端 `main`）各自按 scope 提交并推送
