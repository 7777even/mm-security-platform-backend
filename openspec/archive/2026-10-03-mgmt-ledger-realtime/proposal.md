# Proposal: 通用台账写端点接入实时广播

## 背景
通用台账 25 域行级写能力（P1）已上线，但写端点未标注 `@RealtimeSync`，导致后端 / 大屏 / 移动端对台账的改写不会触发三端同源实时刷新。前端 `MgmtLedgerView` 已在本批接入 `useDomainAutoRefresh('mgmt-ledger', load)`，但后端缺少对应广播，订阅收不到任何变更。

## 目标
为 `MgmtLedgerService` 的 createRow / updateRow / deleteRow 标注 `@RealtimeSync(domain = "mgmt-ledger")`，使其在本事务成功提交后广播 `mgmt-ledger.changed`，与前端订阅闭合。

## 非目标
- 不新增 / 修改任何 HTTP 端点或响应 schema（契约不变，无需四同步）。
- 不动通用台账三张表结构。
