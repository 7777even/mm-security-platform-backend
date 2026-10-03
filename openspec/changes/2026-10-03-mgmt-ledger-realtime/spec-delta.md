# Spec Delta: realtime-broadcast（mgmt-ledger 域）

## 新增广播域
- `mgmt-ledger`：由 `MgmtLedgerService` 的 createRow / updateRow / deleteRow 在事务提交成功后广播 `mgmt-ledger.changed`。

## 不变
- 无新增 / 修改 HTTP 端点，无 schema 变更；契约四同步不适用。
- 既有广播域（alarm / emergency.* / rescue.* / fire-facility.* / security.gate-control / system.* / tv.monitor / video.linkage / form.record 等）不受影响。
