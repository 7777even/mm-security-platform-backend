# 设计：fac_tv_maintenance_order 真实台账

## 迁移（V88，三方言逐字一致）
- `CREATE TABLE fac_tv_maintenance_order`：
  - `id BIGINT AUTO_INCREMENT/BIGSERIAL/NUMBER(19) IDENTITY(1,1) PRIMARY KEY`
  - `order_no VARCHAR(32) NOT NULL`、`device_name VARCHAR(128) NOT NULL`、`device_code VARCHAR(64)`、`fault_desc VARCHAR(256)`、`order_status VARCHAR(16) NOT NULL`（避保留字 `status`）、`assignee VARCHAR(64)`、`department VARCHAR(64)`、`zone_code VARCHAR(32)`、`created_at VARCHAR(32) NOT NULL`、`plan_finish_time VARCHAR(32)`、`actual_finish_time VARCHAR(32)`、`handle_desc VARCHAR(256)`、`sort_no INT DEFAULT 0`。
- `CREATE INDEX idx_tv_maint_order_status ON fac_tv_maintenance_order (order_status)`。
- 种子 45 条：12 PENDING（无负责人/时间/说明）/ 25 PROCESSING（有负责人+计划完成+说明）/ 8 OVERTIME（计划完成已过期、无实际完成）。
- `DELETE FROM fac_tv_stat_item WHERE item_category = 'MAINTENANCE';`（OVERVIEW / EVENT 字典行保留）。
- 仅注释方言标注不同（h2/postgresql/dameng 各一份）。

## 实体与 DTO
- `FacTvMaintenanceOrder`：`@TableName("fac_tv_maintenance_order")`、`@TableId(type=IdType.AUTO) Long id`，字段 `orderNo`/`deviceName`/`deviceCode`/`faultDesc`/`@TableField("order_status") String status`/`assignee`/`department`/`zoneCode`/`createdAt`/`planFinishTime`/`actualFinishTime`/`handleDesc`/`sortNo`。
- `FacTvMaintenanceOrderMapper extends BaseMapper<FacTvMaintenanceOrder>`。
- `TvMaintenanceOrderItem`（`@Data Serializable`）：`id`/`orderNo`/`deviceName`/`deviceCode`/`faultDesc`/`status`/`statusLabel`/`assignee`/`department`/`zoneCode`/`createdAt`/`planFinishTime`/`actualFinishTime`/`handleDesc`。

## 聚合逻辑（TvService.computeOverview）
- 提前 `maintenanceOrderMapper.selectList(null)` → `Collectors.groupingBy(FacTvMaintenanceOrder::getStatus, Collectors.counting())` 得 `orderCount`。
- 概览工单卡片按 `MAINTENANCE_STATUS_ORDER`(PENDING/PROCESSING/OVERTIME) 顺序映射：`label=MAINTENANCE_STATUS_LABEL`、`value=orderCount.getOrDefault(st,0L)`、`tone=MAINTENANCE_STATUS_TONE`。

## 端点（TvController）
- `GET /tv/maintenance-orders?status=`：`tvService.listMaintenanceOrders(status)`（LambdaQueryWrapper `eq` 非空 status，`orderByDesc createdAt`）。
- `GET /tv/maintenance-orders/{id}`：`tvService.getMaintenanceOrder(id)`；缺失抛 `BusinessException(NOT_FOUND)`。
- `toOrderItem(FacTvMaintenanceOrder e)`：映射 `status→statusLabel`（经 `MAINTENANCE_STATUS_LABEL`）。
