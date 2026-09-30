# 契约增量（spec-delta）：fac_tv_monitor.monitor_category

## 后端契约变更（对齐前端 `docs/api/tv.openapi.json` 真源，需四同步）

### 新增字段
- `TvMonitorSummary.monitorCategory: string|null`
  - 监控分类 code：PRODUCTION / BOUNDARY / CLOSED_GATE / OTHER_GATE / OTHER。
- `TvOverviewItem.category: string|null`
  - 分类 code（重大危险源=MAJOR_HAZARD；其余=PRODUCTION/BOUNDARY/CLOSED_GATE/OTHER_GATE/OTHER）。前端据以下钻真实点位；空表示无下钻。
- `TvMonitorUpsertRequest.monitorCategory: string|null`
  - 设备 CRUD 写回字段，落库 `fac_tv_monitor.monitor_category`。

### 受影响端点（契约路径不变，schema 字段新增）
- `GET /api/v1/tv/monitors`（列表）响应 `TvMonitorSummary` 新增 `monitorCategory`。
- `GET /api/v1/tv/overview` 响应 `TvOverviewItem[]` 新增 `category`。
- `POST /api/v1/tv/monitors` / `PUT /api/v1/tv/monitors/{id}` 请求 `TvMonitorUpsertRequest` 新增 `monitorCategory`。

### 不受影响
- `TvOverview` 整体结构不变；重大危险源类仍走既有 hazard 统计。
- 字段均为可选/可空，向后兼容（旧前端不传 `monitorCategory` 不影响写回）。

## 守门
- `scripts/check-api-contract.mjs --strict`：路由 0 差异 / schema 0 漂移（可比 273）。
- 前端 `npm run gen:api-types` 类型 diff 为 0。
