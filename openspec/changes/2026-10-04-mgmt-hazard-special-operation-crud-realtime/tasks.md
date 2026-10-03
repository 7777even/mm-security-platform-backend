# Tasks: 重大危险源 / 监测点位 / 特殊作业票 CRUD 及实时广播

## 实现
- [x] AGENTS.md §6.5 与 commit-msg-lint.sh 的 ALLOWED_SCOPES 同步新增 hazard / special-operation
- [x] V105 三方言迁移为 fac_major_hazard / fac_monitoring_point / fac_special_operation_ticket 增加 version 乐观锁列
- [x] FacMajorHazard / FacMonitoringPoint / FacSpecialOperationTicket 实体补 @Version 字段
- [x] 新增 MajorHazardWriteRequest（10 字段）与 MonitoringPointWriteRequest（8 字段，字符串主键由请求给定）与 SpecialOperationWriteRequest（26 字段）
- [x] HazardService 的 createHazard / updateHazard / deleteHazard 标注 @RealtimeSync(hazard) 并 invalidateAll 对应缓存
- [x] HazardService 的 createPoint / updatePoint / deletePoint 标注 @RealtimeSync(hazard.point) 并 invalidateAll 对应缓存
- [x] 修正 applyPointFields 覆盖主键：改由 createPoint 单独 setId，PUT 不再以请求体 id 改写主键
- [x] SpecialOperationService 的 createTicket / updateTicket / deleteTicket 标注 @RealtimeSync(special-operation)
- [x] HazardController 增加 POST /hazards、PUT 与 DELETE /hazards/{id}、POST /monitoring/points、PUT 与 DELETE /monitoring/points/{id}
- [x] SpecialOperationController 增加 POST /special-operations、PUT 与 DELETE /special-operations/{id}
- [x] hazard.openapi.json 补写端点与 MajorHazardWriteRequest、MonitoringPointWriteRequest、本地 DeleteResult schema（四同步）
- [x] special-operation.openapi.json 补写端点与 SpecialOperationWriteRequest、本地 DeleteResult schema（四同步）

## 验证
- [x] mvn -o compile 通过
- [x] check-endpoint-authz.mjs：写端点 147 个全部具备角色/权限约束
- [x] HazardServiceTest 补 13 例（23 全绿）、SpecialOperationServiceTest 补 6 例（9 全绿）
- [x] check-api-contract.mjs --strict：schema 漂移 0，本批 9 条写路由全部对齐（路由差异维持基线 12）
- [x] 前端 gen:api-types 33 域全成功、type-check 通过、build:subapps 12 子应用全绿
- [ ] 双仓提交并按 scope 推送（后端 hazard / special-operation / openspec，前端 contract / mgmt / docs），关联本 Change 归档
