# Proposal: add-dashboard-alarm-trend

## 问题
前端契约 `dashboard.openapi.json` 已声明 `GET /api/v1/dashboard/alarm-trend`（返回近 24 小时每小时报警数，
`data=AlarmTrendPoint[]`，`AlarmTrendPoint={hour:"08:00", count:int}`），但后端**未实现**（脚本归在「契约有/实现无」14 项前瞻桩中）。
导致大屏报警趋势图无数据源。

## 目标
新增 `GET /api/v1/dashboard/alarm-trend` 端点，返回近 24 小时、按小时分桶的报警计数序列（24 个点，
无报警的小时补 `count:0`），与前端 `AlarmTrendPoint` 字节级对齐。**新增 path + 响应体**，不改既有端点。

## 非目标
- 不新增/修改 `fac_alarm` 表结构（`occurred_at` 已存在，聚合即可）。
- 不做按天/按周/按类型的多维度趋势（属后续 Change，前端当前仅要按小时）。
- 不动 alarm CRUD（POST/PUT/DELETE）、risk-heatmap 等其他前瞻桩。

## 影响面
- 新增 `dto/AlarmTrendPoint.java`（hour/count，对齐前端）。
- `DashboardService` 新增 `trend24h(LocalDateTime now)`：用已有 `AlarmMapper` 查 `fac_alarm`(deleted=0,
  occurred_at ∈ 近24h) 并在 Java 侧按小时分桶（避免 SQL `DATE_FORMAT` 方言差异，沿用 AGENTS §6.4 不写死方言）。
- `DashboardController` 新增 `@GetMapping("/alarm-trend")`，返回 `Result<List<AlarmTrendPoint>>`。
- 测试：断言 24 点、各小时 count、`count:0` 补位。
