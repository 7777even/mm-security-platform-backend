# Spec Delta: 周界告警 LIST + 维保子写端点（后端实现）

## Capability: security（后端实现）

### ADDED — 周界入侵告警 LIST 端点

- 后端 SHALL 暴露 `GET /api/v1/security/perimeter-alarms`（`SecurityController` + `SecurityService.listPerimeterAlarms`），
  按 `alarm_time desc, id desc` 拉全量 `fac_perimeter_alarm`，map 为 `PerimeterAlarmDetail[]` 经 B3 包络返回。
- `SecurityService.java` SHALL 导入 `java.util.stream.Collectors`（`listPerimeterAlarms` 使用 `Collectors.toList()`）。

## Capability: fire-facility（后端实现）

### ADDED — 维保子写请求 DTO

- 后端 SHALL 提供 `FireFacilityMaintenanceWriteRequest`（`date`/`content` 必填，`reportFile` 可空），
  不含任何设备实时状态字段（status 不进写 DTO，零下行控制红线）。

### CHANGED — 维保记录 DTO 携带主键

- `FireFacilityMaintenanceRecord` SHALL 增加 `id`（记录主键）/ `ledgerId`（所属台账 id）字段，供前端单独删除引用与回显。

### ADDED — 维保子写端点

- 后端 SHALL 暴露 `POST /api/v1/fire-facility/ledger/{ledgerId}/maintenance`
  （`@RequireAuth(perm="fire-facility:ledger:write")`，`@RealtimeSync(domain="fire-facility.ledger")`，
  `sort_no` 按该台账内 `max+1`，规避 H2/PG/DM 序列滞后撞主键）与
  `DELETE /api/v1/fire-facility/maintenance/{recordId}`（`@RealtimeSync(domain="fire-facility.ledger")`）。
- `FireFacilityService.createMaintenance`：校验 ledger 存在（`NOT_FOUND` 否则）、`date`/`content` 非空；
  `deleteMaintenance`：记录不存在返回 `NOT_FOUND`，否则物理删除；成功删除返回 `Result.ok(null)`。

#### Scenario: 周界告警 LIST 有序

- **WHEN** 调用 `GET /api/v1/security/perimeter-alarms`
- **THEN** 返回按告警时间倒序的全部告警；`data[0].alarmCode` SHALL 为较新告警、`data[1]` 为较旧告警

#### Scenario: 维保子写落库并广播

- **GIVEN** ledgerId=1 存在且其维保记录最大 `sort_no` 为 N
- **WHEN** `POST /api/v1/fire-facility/ledger/1/maintenance` 携带合法 `date`/`content`
- **THEN** 返回记录 `id>0`、`ledgerId=1`、插入 `sort_no=N+1`；`fire-facility.ledger` 域触发刷新
- **AND** `DELETE /api/v1/fire-facility/maintenance/{recordId}` 对不存在记录返回 `NOT_FOUND`，对存在记录返回 `code=0` 且 `data=null`

#### Scenario: 跨库守门零新增漂移

- **GIVEN** 后端已实现 LIST/维保子写端点且前端契约已对齐
- **WHEN** 执行 `node scripts/check-api-contract.mjs --strict`
- **THEN** schema 漂移 SHALL 为 0；本次新增路由 SHALL 全部落入「已对齐」集合（存量 12 差异为 gate/bollard/patrol 技术债）
