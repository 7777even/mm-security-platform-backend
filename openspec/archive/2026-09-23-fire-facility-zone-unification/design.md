# Design: 消防监测数据真源归一

## 数据模型

### fac_fire_facility_monitor（V72 改造）

原 12 行（按设施类型）→ 改为 **168 行（14 区 × 12 类型）**，新增列：

- `zone_code VARCHAR(32) NOT NULL DEFAULT ''`
- `zone_name VARCHAR(64) NOT NULL DEFAULT ''`

每行 = 某装置区某类设施的 (total/online/offline/fault/monitor_status/last_report_time)。

**守恒约束（由生成脚本保证）：**
- 按 `key_code` 聚合并 `SUM(total_count)` = 原 12 类值（监测卡片不变，总数 983）。
- 按 `zone_code` 聚合并 `SUM(total_count)` = 983（14 区之和，与监测总数一致）。
- 每区设备数：92/68/78/81/72/48/102/75/90/63/60/52/57/45。

### fac_fire_facility_param（V72 改造）

新增 `key_code VARCHAR(32) NOT NULL DEFAULT ''`，**参数关联从 `monitor_id` 改为 `key_code`**（一对多行后参数属类型级）。V72 用 CASE 按 `monitor_id 1..12 → key_code` 回填既有 29 条参数。

## 服务层

### FireFacilityService.monitors()

`selectList` 后按 `key_code` 聚合（SUM total/online/offline/fault，取首个 row 的 status/last_report_time/sort_no 排序），参数按 `key_code` 分组挂接。DTO `FireFacilityMonitorSummary` 形态不变 → 前端 12 卡不变。

### FireFacilityService.reportMonitors()

`selectOne(key_code)` → `selectList(key_code)`，对返回的全部行广播同一份类型级计数并 `updateById`；参数 `DELETE/INSERT` 改由 `key_code`。新增 key 时插入单行（`zone_code=''`）。

### FireSituationService.areaSummary()

注入 `FacFireFacilityMonitorMapper`，按 `zone_code` 聚合 `SUM(total_count)` 得 `equipByZone`；`toAreaItem` 用 `equipByZone.getOrDefault(areaCode,0)` 代替原 `row.getEquipment()`。装置区卡片其余字段（cameras/personnel/status）仍来自 `fac_fire_monitor_area`。

## 迁移幂等

V72 为新增迁移版本，不与 V20 冲突（Flyway 校验和不受影响）。`DELETE FROM fac_fire_facility_monitor` 后重插矩阵；`fac_fire_monitor_area` 不动。三方言（h2/dameng/postgresql）ALTER 语法差异已处理（H2/PG `ADD COLUMN`，DM `ADD`）。
