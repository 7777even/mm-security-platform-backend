# Design: 周界告警 LIST + 维保子写端点（后端实现）

## 复用既有范式

- 周界告警 CRUD 的「写回/删除/LIST」统一由 `SecurityService` + `SecurityController` 承载，
  LIST 仅新增一个只读聚合方法，复用 `toPerimeterAlarmDetail` 适配，不新增 DTO。
- 维保子写复用台账写接口的 `@RealtimeSync(domain="fire-facility.ledger")` 广播域，
  使管理端「消防设施台账」与「维保记录」在同一订阅下保持一致，避免另开广播域。

## 维保子写 id / sort_no 策略（对齐铁律）

- 三方言（H2/PG/DM）自增序列滞后：迁移显式 `INSERT (id,...)` 后序列不推进，新增从 1 撞主键会 409「数据冲突」。
  本项目统一用 `LedgerIdSupport.nextId(...)` / `nextSortNo(...)` 显式 `max+1`，三方言一致、零迁移。
- `createMaintenance`：`sort_no` = 该 `ledgerId` 下现有 `max(sort_no)+1`（无记录则从 1 起），不依赖数据库序列。

## 零下行控制红线

- 维保子写是物理台账写回（落 `fac_fire_facility_maintenance`），非设备下行控制；写请求体只含 `date`/`content`/
  `reportFile`，不含任何设备实时状态字段（status 不进写 DTO）。

## B3 包络

- 参数/JSON 校验失败 → HTTP 200 + `code!=0`（非 400），与其他写接口一致。
- 记录不存在 → `NOT_FOUND`（code=404 语义），`Result.ok(null)` 仅在成功删除时返回。

## 编译修复

- `SecurityService.listPerimeterAlarms` 使用 `Collectors.toList()` 但类未导入 `java.util.stream.Collectors`，
  本次补导入；否则 `mvn compile` 报「找不到符号 Collectors」。
