# Tasks: 应急预案主记录台账写端点（后端）

- [x] `EmergencyPlanMetaItem` / `EmergencyPlanMetaWriteRequest` DTO（补 tabKey）
- [x] `EmergencyPlanService` 4 写方法 + 广播域 `emergency.plan`
- [x] `EmergencyPlanController` 4 端点 + 权限码 `emergency:plan:write`
- [x] Flyway V98 三方言权限种子（sort_order 139）
- [x] `mvn compile` 通过
- [x] 契约守门 `check-api-contract.mjs --strict` 0 差异
- [x] 双仓提交推送并归档
