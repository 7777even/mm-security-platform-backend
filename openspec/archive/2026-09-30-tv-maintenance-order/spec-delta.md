# 契约增量（spec-delta）：fac_tv_maintenance_order

## 后端契约变更（对齐前端 `docs/api/tv.openapi.json` 真源，需四同步）

### 新增 schema
- `TvMaintenanceOrderItem`：工单明细项（维修工单下钻真源）
  - `id: integer`，`orderNo: string`（工单编号 WO-2026-xxxx），`deviceName: string`（设备/点位名称），`deviceCode?: string`（设备编码），`faultDesc?: string`（故障描述），`status?: string`（PENDING/PROCESSING/OVERTIME），`statusLabel?: string`（未接单/处理中/已超时），`assignee?: string`（派单人/负责人），`department?: string`（责任部门），`zoneCode?: string`（防区编码），`createdAt?: string`（创建时间），`planFinishTime?: string`（计划完成时间），`actualFinishTime?: string`（实际完成时间），`handleDesc?: string`（处理说明）。

### 新增端点
- `GET /api/v1/tv/maintenance-orders`
  - query: `status?: string`（PENDING/PROCESSING/OVERTIME；空=全部）
  - 200: `Result<List<TvMaintenanceOrderItem>>`（`List` 可能为空）
- `GET /api/v1/tv/maintenance-orders/{id}`
  - 200: `Result<TvMaintenanceOrderItem>`
  - 404: 工单不存在（`BusinessException(NOT_FOUND)`）

### 受影响端点
- `GET /api/v1/tv/overview`：响应 `TvOverview.maintenanceOrders[]` 计数来源由 `fac_tv_stat_item.MAINTENANCE` 切换为 `fac_tv_maintenance_order GROUP BY order_status`（结构 `TvMaintenanceOrder[]` 不变，仅数据真源变更）。

### 不受影响
- `TvMaintenanceOrder`（概览卡片结构：label/value/tone）字段不变；仅 value 数据真源变更。
- 删除的 `fac_tv_stat_item.MAINTENANCE` 字典行不对外暴露。

## 守门
- `scripts/check-api-contract.mjs --strict`：路由 0 差异 / schema 0 漂移（可比 274）。
- 前端 `npm run gen:api-types` 类型 diff 为 0。
