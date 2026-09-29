# Design: 工业电视防区联动与设备/历史回放（tv-zone-linkage）

## 目标与约束
- 目标：① 打通「生产告警 ↔ 工业电视抓拍」跨域关联（Task 1）；② 工业电视设备/快照带防区归属，支持按防区筛选与历史回放（Task 2，L4 库结构）；③ 提供设备列表与单设备快照分页端点，支撑前端二级回放页。
- 硬约束：零下行控制红线（不新增任何下行硬控写端点）；B3 包络不变（HTTP 200 + code）；权限码沿用（`video:snapshot:create` / `video:snapshot:ack`）；新只读端点复用类级 `@RequireAuth`。

## 架构与方案

### 1. L4 库结构（V86 三方言迁移）
- `fac_tv_monitor` 增加 `zone_code VARCHAR(32)`（达梦 `VARCHAR2(32 CHAR)`），FK 语义关联 `sys_zone.zone_code`（逻辑关联，不建物理 FK 以兼容方言）。
- `fac_tv_snapshot` 增加 `zone_code VARCHAR(32)`。
- 种子回填：
  - h2：`UPDATE fac_tv_monitor SET zone_code = <按 monitor_code 语义映射>`；`UPDATE fac_tv_snapshot s SET zone_code = (SELECT m.zone_code FROM fac_tv_monitor m WHERE m.monitor_code = s.monitor_code)`。
  - postgresql：`UPDATE fac_tv_monitor SET zone_code = ...`（同语义，注意 `UPDATE ... SET (col) = (subquery)` 语法兼容）。
  - dameng（DM8）：无 `ADD COLUMN` 关键字写法差异（DM8 支持 `ALTER TABLE ... ADD (zone_code VARCHAR2(32 CHAR))`）；布尔/`NUMBER(1)` 不适用此处；回填 SQL 与 h2 同义。
- 防区映射规则（语义分配，7 区）：按 `monitor_code` 前缀 / 点位命名把设备归属到炼油/乙烯/罐区/仓储/码头/芳烃/特勤保障；无明确语义的归「特勤保障区」兜底。

### 2. 跨域告警关联抓拍（Task 1）
- `TvSnapshotIngestRequest` 增 `alarmId`(Long) / `alarmType`(String：PRODUCTION/FIRE/PERIMETER)。
- `TvService.submitSnapshots` 落库时写入 `fac_tv_snapshot.alarm_id` / `alarm_type`；`TvService.ingestOne` 回填。
- 列表 `GET /tv/snapshots` 增加 `alarmId` / `alarmType` 等值过滤（MyBatis-Plus `QueryWrapper`）。

### 3. 设备与历史回放端点（Task 2）
- `GET /tv/monitors` → `List<TvMonitorSummary>`：从 `fac_tv_monitor` 全量读取，join `sys_zone` 得 `zoneName`。
- `GET /tv/monitors/{code}/snapshots` → `TvSnapshotPage`：按 `monitor_code` 分页查 `fac_tv_snapshot`，支持 `startTime` / `endTime` 区间。
- `GET /tv/snapshots` 扩展：`monitorCode` / `zone` / `startTime` / `endTime` 过滤（原 `alarmId` / `alarmType` 保留）。
- DTO：`TvMonitorSummary`(新增：code/name/online/department/zoneCode/zoneName)、`TvMonitorDetail`（增 zoneCode/zoneName）、`TvSnapshotItem`（增 zoneCode/zoneName/alarmId/alarmType）。

## 决策记录（ADR）
- ADR-1 防区存 `zone_code` 而非 `zone_name`：遵循用户选型 B（新增关联列），保持与 `sys_zone` 主数据一致，名称经 join 取，避免名称冗余与不一致。
- ADR-2 快照冗余存 `zone_code`：抓拍写入时按点位回查 `zone_code` 落库，回放查询可免 join 直接按 zone 过滤，读性能更优；与 monitor 表存 `zone_code` 一致。
- ADR-3 不新增下行写端点：回放页为纯只读查询，沿用零下行控制红线；设备/防区筛选在前端完成（下拉复用 `GET /system/zones` + `GET /tv/monitors`）。

## 风险与缓解
| 风险 | 可能影响 | 缓解 |
| --- | --- | --- |
| 三方言迁移语法差异（DM8 / PG） | 迁移在达梦/PG 失败 | V86 按方言分别编写；h2 通过 Flyway 实跑；PG/DM 静态对拍 + 真机（若有）验证 |
| 存量快照 `zone_code` 回填遗漏 | 部分老快照无防区 | 种子脚本按 monitor_code 回查回填；空则留 NULL（前端按「未知防区」展示，不崩） |
| 新增端点被契约守门误报差异 | CI 红 | `check-api-contract.mjs --strict` 已归零；`tv.openapi.json` 四铁律 PASS |

## 依赖
- 上游：既有 `TvController` / `TvService` / `FacTvSnapshot` / `FacTvMonitor`；`sys_zone` 主数据（7 区，已有 `GET /api/v1/system/zones`）。
- 下游：前端 `tv.openapi.json` 契约 + `src/services/tv.ts` + `views/tv/playback.vue` 二级回放页（前端 Change `tv-zone-linkage`）。
