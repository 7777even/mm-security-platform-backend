# Tasks: 通用台账 25 域行级写能力（后端实现）

## 实现（Task 1）
- [x] `MgmtLedgerService` 新增 createRow/updateRow/deleteRow（@Transactional，LedgerIdSupport 分配 id/sortNo）
- [x] `MgmtLedgerController` 新增 POST/PUT/DELETE /{domain}/rows[/rowId]，@RequireAuth(role=ADMIN)
- [x] `list()` 返回与显示行对齐的 rowIds（RowView 同切片抽取）
- [x] 新增 DTO MgmtLedgerRowWriteRequest / MgmtLedgerCellWriteDto；MgmtLedgerListResult 增 rowIds

## 契约守门（Task 2）
- [x] 前端 `docs/api/mgmt-ledger.openapi.json` 已补 3 写端点 + 2 schema + list 结果 rowIds（前端 Change 侧）
- [x] `node scripts/check-api-contract.mjs --strict`：mgmt-ledger 路由差异 0 / schema 漂移 0

## 验证（Task 3）
- [x] `mvn -o compile` 通过
- [x] 双仓提交并按 scope 拆分推送（后端 common / 前端 mgmt + contract），关联本 Change 归档
