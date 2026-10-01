# Tasks: 消防设施故障全量 CRUD（后端）

- [x] 新增 DTO `FireFacilityFaultCreateRequest`（必填 5 + 可选 13）
- [x] 扩展 DTO `FireFacilityFaultUpdateRequest`：追加 8 个基础字段（可选）
- [x] Service `createFault`：校验 + 编号唯一 + sort_no 接续 + `@RealtimeSync`
- [x] Service `deleteFault`：级联删时间线 + 物理删除 + `@RealtimeSync`
- [x] Service `updateFault`：追加基础字段局部更新 + 级别校验
- [x] Controller：`POST /faults`、`DELETE /faults/{faultId}`（含 `@RequireAuth` 权限码）
- [x] 迁移 V91 三方言（h2 / dameng / postgresql）登记权限码并授权
- [x] 单测：`FireFacilityServiceTest` 增 6 例、`FireFacilityControllerTest` 增 2 例（全绿）
- [x] `mvn compile` 与方言一致性脚本 PASS；`check-endpoint-authz` PASS
- [x] 契约守门 `check-api-contract.mjs --strict`：路由差异 0 / schema 漂移 0
- [x] 真机验证（8787 直连）：新增成功 / 重复编号 409 / 全字段 PUT / 非法级别 100 / 删除 / 再删 404 / 条数回正
- [x] 双仓提交推送并归档
