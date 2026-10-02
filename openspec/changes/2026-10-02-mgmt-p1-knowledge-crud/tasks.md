# Tasks: 应急知识库台账写端点（后端）

- [x] `KnowledgeWriteRequest` DTO（字段名对齐 `KnowledgeItem`）
- [x] `EmergencyService` 3 个写方法 + 广播域 `emergency.knowledge` + 写后 `invalidateAll`
- [x] `EmergencyController` 3 个端点 + 权限码 `emergency:knowledge:write`
- [x] Flyway V94 三方言权限种子（sort_order 135，避开 V48/V92/V93）
- [x] `mvn compile` 通过
- [x] 契约守门 `check-api-contract.mjs --strict` 0 差异
- [x] 单测：`EmergencyServiceTest` 知识库写侧 6 例
- [x] 真机对拍：8787 直连知识库 CRUD 全路径（16/16 通过）
- [ ] 双仓提交推送并归档
