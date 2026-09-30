# 任务清单：tv monitor_category 真实聚合

## 1. 数据库迁移（V87，三方言）
- [x] `src/main/resources/db/migration/h2/V87__tv_monitor_category.sql`
- [x] `src/main/resources/db/migration/postgresql/V87__tv_monitor_category.sql`
- [x] `src/main/resources/db/migration/dameng/V87__tv_monitor_category.sql`
  （ALTER + 按 monitor_code 打标 + CREATE INDEX，三方言逐字一致）

## 2. 实体与 DTO
- [x] `FacTvMonitor` 增加 `monitorCategory`（`@TableField("monitor_category")`）
- [x] `TvMonitorSummary` 增加 `monitorCategory`
- [x] `TvOverviewItem` 增加 `category`
- [x] `TvMonitorUpsertRequest` 增加 `monitorCategory`

## 3. 聚合逻辑
- [x] `TvService.computeOverview` 改为 GROUP BY category 实时计数（重大危险源仍走 hazardCount）
- [x] `listMonitors` / `toMonitorSummary` 透传 `monitorCategory`
- [x] 写回 `toMonitorEntity` / `updateMonitor` 同步 `monitorCategory`（更新非空才覆盖）

## 4. 测试
- [x] `TvServiceTest.overview_splitsCategoriesAndMapsStats` 重构：mock 13 点位 + 6 OVERVIEW 项，断言各类实时计数与 total=13
- [x] `mvn test` 全绿（基线 + 新增，jacoco 覆盖率达标）

## 5. 契约四同步
- [x] 前端 `docs/api/tv.openapi.json` 加 `TvMonitorSummary.monitorCategory` / `TvOverviewItem.category` / `TvMonitorUpsertRequest.monitorCategory`
- [x] `npm run gen:api-types` 重新生成 TS 类型
- [x] `scripts/check-api-contract.mjs --strict`：路由 0 差异 / schema 0 漂移（可比 273）
