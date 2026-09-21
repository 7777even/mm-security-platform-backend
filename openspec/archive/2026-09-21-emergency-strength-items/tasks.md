# Tasks: 应急力量「无明细」三类别补真实参考数据

> 唯一任务真源；完成即勾选（`[x]`），全勾后同交付内归档。

## 契约
- [x] 1. `emergency.openapi.json` 的 `EmergencyResource.items` 描述更新（覆盖应急场所/医疗机构/消防设施，仅装备车辆为 null）
- [x] 2. `EmergencyResource.items` example 增补消防设施/应急场所示例
- [x] 3. 前端四条铁律校验（`validate-api-contracts.mjs`）通过

## 后端实现
- [x] 4. 新实体 `SysEmergencyStrengthItem`（kind/name/meta/sortNo）+ `SysEmergencyStrengthItemMapper`
- [x] 5. Flyway V61 三方言：建 `sys_emergency_strength_item` + 种子应急场所(6)/医疗机构(3)
- [x] 6. `EmergencyService` 注入 `SysEmergencyStrengthItemMapper` + `FacFireFacilityLedgerMapper`
- [x] 7. `strengthItemsFromLedger` 增加 应急场所/医疗机构(参考表)、消防设施(真实台账) 分支
- [x] 8. `strengthCountFromLedger` 增加 消防设施 计数覆盖
- [x] 9. `check-dialect-migration-consistency.py` 三方言通过
- [x] 10. `check-api-contract.mjs --strict` 路由/schema 0 差异

## 测试
- [x] 11. `EmergencyServiceTest` 加两 mapper mock；原 应急场所 items==null 改非 null；新增 消防设施/医疗机构 断言
- [x] 12. `mvn test` 对应用例全绿

## 收尾
- [x] 13. 合并 spec-delta 入 `specs/emergency-reference/spec.md` 并归档本 Change
- [x] 14. 双仓按 scope 提交推送（后端 db/common；前端 contract）
