# Tasks

## 1. L4 库结构（V86 三方言迁移）
- [x] 新增 `h2/V86__tv_zone_linkage.sql`：`fac_tv_monitor` / `fac_tv_snapshot` 加 `zone_code` + 种子回填（按 monitor_code 语义映射防区；快照按点位回查）
- [x] 新增 `postgresql/V86__tv_zone_linkage.sql`（同语义，PG 语法兼容）
- [x] 新增 `dameng/V86__tv_zone_linkage.sql`（DM8：`VARCHAR2(32 CHAR)`，无 ADD COLUMN 语法差异）
- [x] `FacTvMonitor` / `FacTvSnapshot` 加 `zoneCode` 字段 + `@TableField("zone_code")`

## 2. 跨域告警关联抓拍（Task 1）
- [x] `TvSnapshotIngestRequest` 增 `alarmId`(Long) / `alarmType`(String)
- [x] `TvService.submitSnapshots/ingestOne` 落库 `alarm_id` / `alarm_type`
- [x] `GET /tv/snapshots` 增加 `alarmId` / `alarmType` 等值过滤

## 3. 设备与历史回放端点（Task 2）
- [x] 新增 `GET /tv/monitors` → `List<TvMonitorSummary>`（join `sys_zone` 取 zoneName）
- [x] 新增 `GET /tv/monitors/{code}/snapshots` → `TvSnapshotPage`（支持 startTime/endTime）
- [x] `GET /tv/snapshots` 扩展 `monitorCode` / `zone` / `startTime` / `endTime` 过滤
- [x] 新增 `TvMonitorSummary` DTO；`TvMonitorDetail` / `TvSnapshotItem` 增 `zoneCode` / `zoneName`（+ 快照 `alarmId` / `alarmType`）

## 4. 契约与守门（跨库四同步）
- [x] 前端 `tv.openapi.json` 同步：`TvSnapshotIngestRequest` 补 `alarmId`/`alarmType`；`TvMonitorDetail`/`TvSnapshotItem` 补防区与告警字段；新增 `TvMonitorSummary` schema；新增 `/tv/monitors`、`/tv/monitors/{code}/snapshots` 路径；`/tv/snapshots` 增过滤参数（四铁律：注释/中文 description/example 齐全）
- [x] 前端 `npm run gen:api-types` 重生成（TvSnapshotIngestRequest 等类型已含新字段）
- [x] 前端 `node scripts/validate-api-contracts.mjs` PASS（33 域）
- [x] 前端 `npm run type-check`（vue-tsc）PASS
- [x] 后端 `node scripts/check-api-contract.mjs --strict` 路由差异 0 / schema 漂移 0（EXIT=0）

## 5. 验证与归档
- [x] 前端 `views/tv/playback.vue` 二级回放页（设备/防区筛选 + 快照网格 + 分页）已落地，`router` + `fmRouteNameMap` 已接线
- [x] 后端 `mvn compile` 0 错误
- [x] spec-delta 合入 `openspec/specs/tv/spec.md`（AMEND 三条 Requirement）
- [x] 归档 `git mv` 到 `openspec/archive/2026-09-29-tv-zone-linkage`
- [x] `node scripts/check-openspec-hygiene.mjs` 通过
