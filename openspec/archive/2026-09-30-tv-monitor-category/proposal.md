# 提案：工业电视概览真实聚合（fac_tv_monitor.monitor_category）

> **状态：`done` —— 已实现并验证（后端单测 18 绿、契约守门 0 漂移）。归档 Change。**
> 配套前端 Change：`frontend-scaffold/openspec/archive/2026-09-30-tv-monitor-category`。

## 背景
工业电视大屏「视频监控概览」卡片数字来自 `fac_tv_stat_item` 字典手填值，真实设备表 `fac_tv_monitor` 无"类别"列，两表无关联键。用户反馈"面板数据哪里来的也不知道"——详情与真实点位未联通（原"其它"类只把统计数字再念一遍，无逐点明细）。

## 目标（B 方案 · 后端根治）
1. 给真实表 `fac_tv_monitor` 加 `monitor_category` 列（V87 三方言），并对现有 15 个种子点位打标。
2. `TvService.computeOverview()` 改为按 `fac_tv_monitor.monitor_category` 实时 `GROUP BY` 计数；重大危险源仍走既有 hazard 统计。
3. DTO 暴露分类字段：
   - `TvMonitorSummary.monitorCategory`（列表/详情展示）
   - `TvOverviewItem.category`（概览下钻键）
   - `TvMonitorUpsertRequest.monitorCategory`（设备 CRUD 写回）
4. 跨库四同步：前端 `tv.openapi.json` 加同名字段 → `npm run gen:api-types` → 契约守门 0 漂移。

## 非目标（本期不做）
- 不改动 `fac_tv_stat_item` 字典表（仍作为重大危险源等无逐点明细类的兜底手填值）。
- 不引入新概览分类（沿用 PRODUCTION/BOUNDARY/CLOSED_GATE/OTHER_GATE/OTHER + MAJOR_HAZARD）。

## ADR
- **ADR-1 分类真源**：`fac_tv_monitor.monitor_category` 为真实聚合真源；`fac_tv_stat_item` 仅作无明细类的兜底。
- **ADR-2 code 约定**：PRODUCTION/BOUNDARY/CLOSED_GATE/OTHER_GATE/OTHER（实体列值）；`TvService.OVERVIEW_LABEL_CATEGORY` 映射中文化 label → code。
- **ADR-3 缓存**：`computeOverview` 既有 Caffeine 10s TTL 不变，实时计数随点位变更自动失效刷新。

## 风险
- 三方言迁移须一致（h2/postgresql/dameng 均 ALTER + UPDATE 打标 + CREATE INDEX）。本机无 Docker 未跑真实容器 IT，H2 通过不代表达梦/PG 通过（已如实标注）。
- 既有前端未传 `monitorCategory` 的写回请求：更新时 `if (req.getMonitorCategory() != null)` 才覆盖，避免清空已有打标。
