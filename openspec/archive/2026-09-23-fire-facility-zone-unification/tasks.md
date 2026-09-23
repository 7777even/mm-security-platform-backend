# Tasks: 消防监测数据真源归一

- [x] V72 三方言迁移：fac_fire_facility_monitor 加 zone_code/zone_name，改为 (区×类型) 矩阵
- [x] V72 三方言迁移：fac_fire_facility_param 加 key_code 并回填既有参数
- [x] 实体 FacFireFacilityMonitor 加 zoneCode/zoneName（@TableField）
- [x] 实体 FacFireFacilityParam 加 keyCode（@TableField）
- [x] FireFacilityService.monitors() 按 key_code 聚合回 12 类卡片
- [x] FireFacilityService.reportMonitors() 按 key_code 更新全部行、参数改由 key_code 关联
- [x] FireSituationService.areaSummary() 按 zone_code 聚合监测表得出区设备数
- [x] 同步更新 FireFacilityServiceTest / FireFacilityMonitorReportTest 参数关联改为 key_code
- [x] 方言一致性检查通过（--dialects h2,dameng,postgresql）
- [x] 后端相关单测通过（FireFacilityServiceTest / FireFacilityMonitorReportTest / FireMonitoringServiceTest）
- [x] 重启后端使 V72 生效并 curl 验证：areas 各区之和=983、equipment-status 仍 983/20/7、monitors 仍 12 类=983
- [x] 归档本变更（git mv → openspec/archive）并推送
