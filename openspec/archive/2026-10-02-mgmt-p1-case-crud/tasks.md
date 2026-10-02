# Tasks: 事故案例库台账写端点（后端）

- [x] `fac_emergency_case` 实体 / Mapper / DTO（Item/WriteRequest/List）
- [x] `EmergencyService` 3 写方法 + 广播域 `emergency.case`
- [x] `EmergencyController` 3 端点 + 权限码 `emergency:case:write`
- [x] Flyway V96 三方言建表 + 权限种子（sort_order 137）+ 2 条示例
- [x] `mvn compile` 通过
- [x] 契约守门 `check-api-contract.mjs --strict` 0 差异
- [x] 双仓提交推送并归档
