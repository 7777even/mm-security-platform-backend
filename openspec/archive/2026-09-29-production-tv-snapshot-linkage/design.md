# Design: 生产告警 ↔ 工业电视抓拍数据级联通（production-tv-snapshot-linkage）

## 目标与约束
- 目标：生产告警详情面板内嵌「现场工业电视抓拍」区块在「无显式 alarm_id 绑定」时也能展示关联抓拍——通过时间窗 + 位置关键词的自动兜底关联，把符合条件的 `fac_tv_snapshot` 反写 `alarm_id/alarm_type` 再返回。
- 硬约束：零下行控制红线（仅读 + 关联反写，不触达物理设备）；B3 包络不变；复用既有读/采集权限（无新权限码）；关联事实真源留在抓拍侧（`fac_production_alarm` 不反向加字段）。

## 架构与方案

### 1. 自动关联兜底算法（TvService.autoRelateSnapshotsForAlarm）
- 入参：`alarmId, alarmType, location`(告警位置), `occurredAt`(告警发生时刻)。
- 候选集：`fac_tv_snapshot` 中 `alarm_id IS NULL` 且 `capture_time ∈ [occurredAt-15min, occurredAt+15min]`。
- 位置匹配：双向包含——`location` 包含 `(monitorName+zoneName)` 任一，或 `(monitorName+zoneName)` 包含 `location`，即命中；避免单向误关联。
- 反写：命中行 `UPDATE alarm_id=alarmId, alarm_type=alarmType`；命中即失效 `snapshotListCache`。
- 幂等：仅处理 `alarm_id IS NULL` 候选；已关联不重复。

### 2. 端点兜底（ProductionController.alarmSnapshots）
- `GET /api/v1/production/alarms/{id}/snapshots`：先查显式关联（`alarm_id=id AND alarm_type='PRODUCTION'`）；为空则调 `autoRelateSnapshotsForAlarm` 兜底再查；仍空返回 `total=0` 空列表（前端渲染空态，绝不编造）。

### 3. dev 种子（TvSnapshotSeeder @Profile("dev")）
- 给前 2 条生产告警各生成 1 张绑定样例抓拍（`alarm_id` 显式填，喂给详情区块），便于 dev 直接看到关联效果。

## 决策记录（ADR）
- ADR-1 关联兜底放在查询端而非采集端：采集侧已支持 `submitSnapshots` 带 `alarmId`，但历史/未带 `alarmId` 的抓拍需查询时反写，故在 GET 端点兜底，保证存量数据可用。
- ADR-2 位置双向包含而非等值：工业点位命名与告警位置字段口径不同（如「炼油装置区」vs「炼油一装置」），双向包含兼顾召回与误关联控制。

## 风险与缓解
| 风险 | 缓解 |
| --- | --- |
| ±15min 窗过宽/过窄 | 与用户确认取 ±15min；可调常量 |
| 位置关键词不匹配导致恒空 | dev 种子显式绑定兜底可视化；负例走空态 |
| 自动反写分支零测试 | 本期补 `TvServiceTest` 验证窗口内反写 + 位置不符空（见遗留收口） |

## 依赖
- 上游：`FacTvSnapshot` / `TvService` / `ProductionController`；既有 `GET /production/alarms/{id}/snapshots`。
- 下游：前端 `AlarmDetailPanel` 抓拍区块 + `fetchProductionAlarmSnapshots`。
