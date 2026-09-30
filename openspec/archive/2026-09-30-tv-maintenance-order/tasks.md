# 任务清单：tv maintenance_order 真实台账

## 1. 数据库迁移（V88，三方言）
- [x] `src/main/resources/db/migration/h2/V88__tv_maintenance_order.sql`
- [x] `src/main/resources/db/migration/postgresql/V88__tv_maintenance_order.sql`
- [x] `src/main/resources/db/migration/dameng/V88__tv_maintenance_order.sql`
  （CREATE TABLE + INDEX + 45 种子 + DELETE MAINTENANCE 字典行，三方言逐字一致；`order_status` 避保留字）

## 2. 实体 / Mapper / DTO
- [x] `FacTvMaintenanceOrder`（`@TableName` / `@TableField("order_status")` / `@TableId(AUTO)`）
- [x] `FacTvMaintenanceOrderMapper extends BaseMapper`
- [x] `TvMaintenanceOrderItem` DTO（14 字段 + statusLabel）

## 3. 聚合逻辑
- [x] `TvService.computeOverview` 改 `GROUP BY order_status` 实时计数（取代 fac_tv_stat_item.MAINTENANCE）
- [x] `MAINTENANCE_STATUS_ORDER / _LABEL / _TONE` 常量（PENDING/PROCESSING/OVERTIME → 未接单/处理中/已超时 → grey/blue/red）
- [x] `listMaintenanceOrders(status)`（LambdaQueryWrapper eq 非空 + orderByDesc createdAt）
- [x] `getMaintenanceOrder(id)` + `toOrderItem(e)`（status→statusLabel 映射）

## 4. 端点（TvController）
- [x] `GET /tv/maintenance-orders?status=`
- [x] `GET /tv/maintenance-orders/{id}`（缺失 NOT_FOUND）

## 5. 测试
- [x] `TvServiceTest.overview_splitsCategoriesAndMapsStats` 重构：mock 12 PENDING + 25 PROCESSING + 8 OVERTIME，断言 3 卡（未接单=12/grey、处理中=25、已超时=8）
- [x] 新增 `listMaintenanceOrders_mapsItemsAndStatusLabel` / `listMaintenanceOrders_mapsStatusLabelAndNullableFields` / `getMaintenanceOrder_returnsNullWhenMissing` / `getMaintenanceOrder_mapsItem`
- [x] `mvn test` 全绿（22 项，jacoco 覆盖率达标）

## 6. 契约四同步
- [x] 前端 `docs/api/tv.openapi.json` 加 `TvMaintenanceOrderItem` schema + `/tv/maintenance-orders` 两路径
- [x] `npm run gen:api-types` 重新生成 TS 类型
- [x] `scripts/check-api-contract.mjs --strict`：路由 0 差异 / schema 0 漂移（可比 274）
