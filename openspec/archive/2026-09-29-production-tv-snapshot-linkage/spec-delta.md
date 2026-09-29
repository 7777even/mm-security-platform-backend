# Spec Delta: production-tv-snapshot-linkage

## Capability: production-alarm

> 生产报警（fac_production_alarm）的查询与详情联动能力。既有 capability 仅覆盖列表/详情/写回；本变更补齐「详情内嵌工业电视关联抓拍」的数据级联通。

### ADDED

#### Requirement: 生产告警关联工业电视抓拍
系统 SHALL 在 `GET /api/v1/production/alarms/{id}/snapshots` 中，当无显式 `alarm_id` 绑定抓拍时，按「发生时刻 ±15 分钟时间窗 + 位置关键词包含」自动将符合条件的 `fac_tv_snapshot` 反写 `alarm_id=id`、`alarm_type='PRODUCTION'`，并返回关联后的抓拍列表；窗口内无候选或位置不符时返回空列表（绝不编造）。

##### Scenario: 告警详情抓拍自动关联（成功）
- **WHEN** 请求某生产告警的关联抓拍，且其发生时刻 ±15 分钟内存在位置关键词匹配的未关联抓拍
- **THEN** 系统反写这些抓拍的 `alarm_id/alarm_type`，返回非空 `TvSnapshotPage`（HTTP 200，B3 包络 `code=0`）

##### Scenario: 窗口内无候选 / 位置不符
- **WHEN** 时间窗内无未关联抓拍，或位置关键词不匹配
- **THEN** 返回 `TvSnapshotPage`（total=0，list 为空），前端渲染空态

##### Scenario: 已关联不再重复处理
- **WHEN** 该告警已存在显式关联抓拍
- **THEN** 直接返回既有关联，不触发自动关联兜底（幂等）
