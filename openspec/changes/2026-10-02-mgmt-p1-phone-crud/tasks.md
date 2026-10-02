# Tasks: 应急通讯录台账写端点（后端）

- [x] `PhoneWriteRequest` DTO（字段名对齐 `EmergencyPhone`）
- [x] `EmergencyService` 3 个写方法 + 广播域 `emergency.phone` + 写后 `invalidateAll`
- [x] `EmergencyController` 3 个端点 + 权限码 `emergency:phone:write`
- [x] Flyway V95 三方言权限种子（sort_order 136，避开 V48/V92/V93/V94）
- [x] `mvn compile` 通过
- [x] 契约守门 `check-api-contract.mjs --strict` 0 差异
- [x] 单测：`EmergencyServiceTest` 通讯录写侧 6 例
- [x] 真机对拍：8787 直连通讯录 CRUD 全路径（17/17 通过）
- [ ] 双仓提交推送并归档
