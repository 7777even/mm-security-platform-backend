# Tasks: 应急事件与流程填报补齐编辑/删除写端点（后端）

- [x] `EmergencyEventUpdateRequest` DTO（局部更新，不含分组维度字段）
- [x] `EmergencyEventService.update`：局部更新 + status 枚举校验 + 双源同步
- [x] `EmergencyEventService.delete`：级联清理详情字段 → 救援行 → 事件本体
- [x] `EmergencyEventService.create/report/startResponse` 补 `@RealtimeSync("emergency.event")`
- [x] `EmergencyEventController`：PUT / DELETE + `emergency:event:write` 权限码
- [x] `FormRecordService.delete` + `FormRecordController` DELETE（ADMIN）+ 三个写方法补 `@RealtimeSync("form.record")`
- [x] 修复 `FormRecordService.create` 主键分配：显式取 `max(id)+1`（V66 种子显式插 id 让自增序列滞后，新增恒定 409）
- [x] Flyway V92 三方言（h2 / dameng / postgresql）权限码种子，sort_order 130 避开 V48/V91
- [x] `mvn compile` 通过
- [x] 契约守门 `check-api-contract.mjs --strict`：路由差异 0 / schema 漂移 0
- [x] 单测：EmergencyEventServiceTest 补 update（4 例）/ delete（2 例），Controller 补路由用例 2 例
- [x] 单测：新增 FormRecordServiceTest（create 主键分配 3 例 + delete 2 例）
- [x] 真机对拍：8787 直连 18/18 通过（新增/编辑/非法状态/不存在/删除/重复删除/列表回正/填报增删）
- [x] 双仓提交推送并归档
