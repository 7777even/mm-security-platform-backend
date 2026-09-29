# Proposal: 工业电视防区联动与设备/历史回放（tv-zone-linkage）

> 状态：`approved` —— L4 库结构变更（含 L3 对外接口扩展）。用户于 2026-09 确认 scope=P0+P1 按优先级补齐，并在「防区数据源」选型中明确选择 **B：新增 `zone_code` 列**（给 `fac_tv_monitor` / `fac_tv_snapshot` 加 `zone_code` 关联 `sys_zone`，属 L4 库结构变更，需三方言迁移 + openspec 提案 + 人工确认门禁）。

## Why
1. **跨域联动需求（Task 1）**：生产告警详情需内嵌关联工业电视抓拍。原 `POST /tv/snapshots` 只能「设备→截图」，无法把截图绑定到某条告警；原 `GET /tv/snapshots` 也无法按告警反查。需在入库请求上支持 `alarmId` / `alarmType`，并在列表提供按告警过滤。
2. **防区归属缺失（Task 2 / L4）**：`fac_tv_monitor` / `fac_tv_snapshot` 无防区归属列，无法按「防区」筛选设备与回放快照。用户选型 B 决定新增 `zone_code` 列关联 `sys_zone`（7 个区：炼油/乙烯/罐区/仓储/码头/芳烃/特勤保障），由种子数据按语义分配。
3. **二级跳转缺失（Task 2）**：工业电视大屏仅有「采集/确认」feed，缺「设备/防区筛选 + 真实历史回放」二级页。需新增设备列表与单设备快照分页端点，并扩展列表过滤参数（monitorCode / zone / startTime / endTime）。

## What Changes
- **L4 库结构（V86 三方言迁移）**：`fac_tv_monitor` 加 `zone_code`（关联 `sys_zone.zone_code`）；`fac_tv_snapshot` 加 `zone_code`。种子数据按点位语义回填防区；存量快照按点位回查防区回填 `zone_code`。
- **入库请求扩展**：`TvSnapshotIngestRequest` 增加可选 `alarmId`（Long）/ `alarmType`（PRODUCTION/FIRE/PERIMETER），落库 `fac_tv_snapshot.alarm_id` / `alarm_type`，实现跨域告警关联抓拍。
- **新端点**：
  - `GET /api/v1/tv/monitors` —— 监控设备摘要列表（`TvMonitorSummary`：code/name/online/department/zoneCode/zoneName）。
  - `GET /api/v1/tv/monitors/{code}/snapshots` —— 单设备快照分页（`TvSnapshotPage`）。
- **列表过滤扩展**：`GET /api/v1/tv/snapshots` 在原有 `page/size/alarmId/alarmType` 基础上增加 `monitorCode` / `zone` / `startTime` / `endTime`，支持设备/防区/时间区间筛选。
- **详情/快照 DTO 防区化**：`TvMonitorDetail` / `TvSnapshotItem` / `TvMonitorSummary` 增加 `zoneCode` / `zoneName`；`TvSnapshotItem` 增加 `alarmId` / `alarmType`。

## Capabilities

### Modified Capabilities
- `tv`：在既有「录像截图采集入库 / 主动采集 / 确认」基础上，扩展「跨域告警关联抓拍」「防区联动归属」「设备与历史回放」能力，并新增 `zone_code` 库结构。

## Impact
- 受影响范围：新增 `V86__tv_zone_linkage.sql`（h2 / postgresql / dameng 三方言）；改造 `TvController` / `TvService` / `FacTvSnapshot` / `FacTvMonitor` / `TvSnapshotIngestRequest` / `TvMonitorDetail` / `TvSnapshotItem` / `TvMonitorSummary`（新增 DTO）。
- 契约同步：前端 `frontend-scaffold/docs/api/tv.openapi.json` 同步新增/扩展（四铁律：按域分组 / 接口有注释 / 字段有中文 description / 有 example）；`TvSnapshotIngestRequest` 补齐 `alarmId` / `alarmType`；新增 `TvMonitorSummary` schema；新增 `/tv/monitors`、`/tv/monitors/{code}/snapshots` 路径；`/tv/snapshots` 增过滤参数。
- 数据影响：新增列，存量数据由种子脚本回填（点位→防区映射、快照按点位回查）。**回退风险**：V86 为 `ADD COLUMN`，回退需 `DROP COLUMN`，无业务数据损失（仅新增归属信息）。
- 安全语义：零下行控制红线不破——全部为只读查询 / 既有采集入库写端点（权限码 `video:snapshot:create` / `video:snapshot:ack` 不变），不新增任何下行硬控写端点。`/tv/monitors`、`/tv/monitors/{code}/snapshots` 复用类级 `@RequireAuth`（登录即可读）。
- 回归面：既有 `POST /tv/snapshots` 入参向后兼容（`alarmId`/`alarmType` 可选，缺省行为不变）；既有 `/tv/snapshots` 列表旧参数不受影响。

## 人工确认关卡（L4 须过）
- [x] 提案范围与用户确认一致：用户于选型阶段确认 scope=P0+P1，并明确选择「新增 zone_code 列」（B 方案，L4 库结构变更）。无需求扩散、无自造平行任务。
- [x] API 契约未违反：零下行控制红线保持；新端点为只读查询 / 既有写端点扩展；B3 包络不变（HTTP 200 + code）。
- [x] 跨库四同步已排定：前端 `tv.openapi.json` 与后端实现同步；前端 Change `tv-zone-linkage` 与后端 `tv-zone-linkage` 同交付；`node scripts/check-api-contract.mjs --strict` 已归零。
- [x] 数据变更影响已确认：新增 `zone_code` 列（L4），存量由种子回填；回退仅需 DROP COLUMN，无业务数据损失；已确认三方言迁移（h2 / postgresql / dameng）。
- [x] 高风险项：未触及 security 权限模型 / 安全过滤器链；仅按用户选型扩展库结构，未引入 zone 过滤下行的硬控逻辑。
