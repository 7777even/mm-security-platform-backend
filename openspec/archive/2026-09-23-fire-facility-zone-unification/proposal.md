# Proposal: 消防监测数据真源归一（装置区设备数）

## 背景 / 问题

大屏存在两套互不相干的消防设备种子数据：

- **装置区卡片**（SafetyAlarmPanel）：`fac_fire_monitor_area.equipment`，14 区合计 **1399**（V38 占位种子，由旧前端硬编码迁入）。
- **消防设施运行监测**（FireFacilityPanel）：`fac_fire_facility_monitor.total_count`，12 类合计 **983**（V20 占位种子）。

两者既不同口径（按区 vs 按设施类型）、又无 zone 维度的勾稽关系，导致「14 区卡片某区 128 台」点进去「设备总数 983」对不上，且两处离线数也互相矛盾。

## 决策（方案 C / 以监测表 983 为准）

将 `fac_fire_facility_monitor` 改为 **(装置区 × 设施类型) 矩阵**，使监测表成为唯一真源：

- 监测卡片按 `key_code` 聚合回 12 类 → 总数仍是 983，面板表现不变。
- 装置区卡片的「消防设备」改为 **按 `zone_code` 聚合监测表** → 14 区之和 = 983，与监测总数自洽。
- 旧 `fac_fire_monitor_area.equipment`（1399 台账口径）不再作为真源使用。

## 影响面

- DB：V72 三方言迁移（加 `zone_code`/`zone_name`；参数表加 `key_code` 并回填；DELETE 旧 12 行并重插矩阵）。
- 后端：`FacFireFacilityMonitor`/`FacFireFacilityParam` 实体加字段；`FireFacilityService.monitors()` 按 `key_code` 聚合；`reportMonitors()` 按 `key_code` 更新全部行、参数改由 `key_code` 关联；`FireSituationService.areaSummary()` 按区聚合监测表。
- 前端：契约 `FireMonitorArea.equipment` 字段形态不变（仅数值变），无需改 TS 类型；openapi 示例值同步更新。

## 风险

- 监测上报（`reportMonitors`）原按单行 key 更新，现一对多行广播同值；真实采集接入时需按 zone+type 区分 key，届时再演进。
- 983/1399 均为占位种子，真源归一后两处数字仍非真实厂区数，待真实采集链路补全后自然被覆盖。
