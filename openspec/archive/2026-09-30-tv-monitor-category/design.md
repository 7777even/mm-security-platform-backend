# 设计：monitor_category 真实聚合

## 迁移（V87，三方言逐字一致）
- `ALTER TABLE fac_tv_monitor ADD COLUMN monitor_category VARCHAR(16)`
- 按 `monitor_code` 打标：
  - `boundary-01~04` → `BOUNDARY`
  - `ar-01/02/03`、`focus-01~04` → `PRODUCTION`
  - `hazard-01/02` → `CLOSED_GATE`
  - `hazard-03` → `OTHER_GATE`
  - `hazard-04` → `OTHER`
- `CREATE INDEX idx_tv_monitor_category ON fac_tv_monitor (monitor_category)`
- 仅注释方言标注不同（h2/postgresql/dameng 各一份）。

## 实体与 DTO
- `FacTvMonitor.monitorCategory`（`@TableField("monitor_category")`）。
- `TvMonitorSummary.monitorCategory` / `TvOverviewItem.category` / `TvMonitorUpsertRequest.monitorCategory`。

## 聚合逻辑（TvService.computeOverview）
- 提前 `listMonitors()` 取全部点位 → `Collectors.groupingBy(FacTvMonitor::getMonitorCategory, Collectors.counting())` 得 `catCount`。
- OVERVIEW 类项：
  - 重大危险源 → 走 `hazardCount`；
  - 其余 → 按 `OVERVIEW_LABEL_CATEGORY` 映射 `category`，实时计数 `catCount.getOrDefault(cat, 0L).intValue()`；
  - 未映射 label → 兜底手填值。

## 写回
- `toMonitorEntity` 写入 `monitorCategory`；`updateMonitor` 同步 `if (req.getMonitorCategory() != null) e.setMonitorCategory(...)`（非空才覆盖）。
- `listMonitors` / `toMonitorSummary` 透传 `monitorCategory`。
