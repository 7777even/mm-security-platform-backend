# Tasks: 应急预案目录台账写端点（后端）

- [x] `EmergencyPlanCatalogRow` / `EmergencyPlanCatalogWriteRequest` DTO
- [x] `EmergencyPlanService` 4 写方法 + 广播域 `emergency.plan-catalog`
- [x] `EmergencyPlanController` 4 端点 + 权限码 `emergency:plan-catalog:write`
- [x] Flyway V97 三方言权限种子（sort_order 138）
- [x] `mvn compile` 通过
- [x] 契约守门 `check-api-contract.mjs --strict` 0 差异
- [x] 双仓提交推送并归档
