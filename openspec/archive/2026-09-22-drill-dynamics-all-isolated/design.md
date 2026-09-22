# Design: 演练事件 12–16 响应动态各自独立展示

## 数据模型（沿用）

- `fac_accident_incident(event_id, title, location, longitude, latitude, hazard_source_level, map_status, started_at, ended_at, status_name, reported, facility_name, is_default)`：V12 建表，V58 已 seed 真实事件 1/2/3/5-9 + 默认事件（event_id=4, is_default=TRUE）；V63 新增演练事件 11。本迁移新增 12–16 五行，`is_default=FALSE`。
- `fac_accident_dynamic(category, title, tag, time, command_text, responder, reply, stage_label, sort_no, incident_id)`：V63 新增 `incident_id` 列 + 索引。本迁移为其各 7 条种子。

## 种子要点

1. **事件行**：`event_id` 12–16 取自 V17 `fac_emergency_event`（drill 行）；`title/location/经纬度` 与 V17 对齐；`hazard_source_level` 取 V17 实际值（均为 NULL）；`map_status` 依 V17 `status` 语义映射（done→演练结束 / processing→演练处置 / pending→待演练）；`started_at/ended_at/status_name/reported` 对齐 V17；`facility_name` 取 location；`is_default=FALSE`。
2. **动态**：每个事件 7 条，category 覆盖 rescue/command/brief/awareness 四类，`sort_no` 1–7，`incident_id` 用子查询 `(SELECT id FROM fac_accident_incident WHERE event_id = <N>)` 引用刚插入行（自增 id，避免与 V58 已占 id 冲突）。
3. **风格**：与 V12 全局参考动态、V63 演练事件 11 一致（中文 `【演练指令】/【快讯】/【快报】` 标签、指令/已回复文案、stage_label 分段）。

## 方言处理

- h2 / postgresql / dameng 三份文件内容完全一致（V64 无 ALTER，全部为单条 INSERT；达梦不支持多行 VALUES，单条写法天然兼容）。
- 已通过 `check-dialect-migration-consistency.py` 三方言静态对拍（版本/表/列对齐，仅预存 BOOLEAN/SMALLINT 类型软警告）。

## 回退与兜底

- 任一演练事件若动态为空（本迁移已保证非空），服务仍按 V63 逻辑回退默认事件动态，不空屏。
- 真实事件若无 `fac_accident_incident` 行，同样回退默认事件。
