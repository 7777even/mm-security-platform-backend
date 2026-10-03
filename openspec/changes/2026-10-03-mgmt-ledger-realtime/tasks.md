# Tasks: 通用台账写端点接入实时广播

## 实现
- [x] `MgmtLedgerService.createRow` 标注 `@RealtimeSync(domain = "mgmt-ledger")`
- [x] `MgmtLedgerService.updateRow` 标注 `@RealtimeSync(domain = "mgmt-ledger")`
- [x] `MgmtLedgerService.deleteRow` 标注 `@RealtimeSync(domain = "mgmt-ledger")`

## 验证
- [x] `mvn -o compile` 通过
- [x] 双仓提交并按 scope 推送（后端 common / 前端 mgmt），关联本 Change 归档
