# Tasks: fire-facility-monitor-report（后端）

- [x] 起草 proposal / design（L3 复用既有表，无 DDL）
- [x] 新增 DTO：FireFacilityMonitorReportRequest / Item / Param
- [x] 实现 FireFacilityService.reportMonitors（upsert + 参数整体替换 + 返回刷新概览）
- [x] 实现控制器端点 POST /fire-facility/monitors/report（perm=fire-facility:handle，@RealtimeSync）
- [ ] 补充单测（控制器端点 + Service upsert 逻辑）并跑 mvn test 全量
- [ ] 跑 check-api-contract.mjs --strict 与 check-endpoint-authz.mjs
- [ ] 同步前端契约 + gen:api-types + 按 scope 拆分双仓提交
