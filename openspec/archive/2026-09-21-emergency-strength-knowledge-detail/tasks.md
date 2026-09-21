# Tasks: 应急力量 / 知识库明细接口富化

> 唯一任务真源；完成即勾选（`[x]`），全勾后同交付内归档。

## 契约
- [x] 1. `emergency.openapi.json` 新增 `StrengthItem` schema
- [x] 2. `EmergencyResource` 加 `items`（含 example）
- [x] 3. `KnowledgeItem` 加 `description`（含 example）
- [x] 4. 前端四铁律校验通过（`validate-api-contracts.mjs`）

## 后端实现
- [x] 5. `StrengthItem` DTO（`name`/`meta`）
- [x] 6. `EmergencyResource` 加 `List<StrengthItem> items`
- [x] 7. `EmergencyService.strength()` 对 4 ledger 类别填明细（LIMIT 20），其余 null
- [x] 8. `KnowledgeItem`/`SysKnowledgeItem` 加 `description`，`knowledge()` 透传
- [x] 9. Flyway V59 三方言（加列 + 种子 3 条）
- [x] 9b. Flyway V60 三方言（补 V35 新增 6 类的种子，V59 已应用不可改校验和）
- [x] 10. `check-dialect-migration-consistency.py` 通过
- [x] 11. `check-api-contract.mjs --strict` 路由/schema 0 差异

## 测试
- [x] 12. `EmergencyServiceTest` 补 strength items + knowledge description 断言
- [x] 13. 后端全量单测全绿

## 前端
- [x] 14. `gen:api-types` 重生成类型
- [x] 15. `services/emergency.ts` / `knowledge.ts` 接口对齐
- [x] 16. `EmergencyRescuePanel` 用真实 items；`SafetyKnowledgePanel` 用真实 description
- [x] 17. `InfoDetailDialog` 支持明细列表区
- [x] 18. 前端 type-check + lint + build:subapps fm-emergency

## 收尾
- [x] 19. 重启后端真机验证两接口
- [x] 20. 双仓按 scope 提交推送；合并 spec-delta 并归档本 Change
