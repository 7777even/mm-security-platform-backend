# Tasks: 救援资源四台账写端点（后端）

- [x] 4 个 WriteRequest DTO（字段名对齐只读 DTO，create / update 共用）
- [x] `RescueResourceService` 12 个写方法 + 4 个广播域 + sort_no 取 max+1
- [x] `RescueResourceController` 12 个端点 + 4 个权限码
- [x] Flyway V93 三方言权限种子（sort_order 131–134，避开 V48/V92）
- [x] 补 `RescuePersonnelItem`（personGroup/phone/dutyStatus）与 `RescueEquipmentItem`（category/unit）
- [x] `mvn compile` 通过
- [x] 契约守门 `check-api-contract.mjs --strict` 0 差异
- [x] 单测：RescueResourceServiceTest 写侧 16 例
- [x] 真机对拍：8787 直连四台账 CRUD 全路径（41/41 通过）
- [x] 双仓提交推送并归档
