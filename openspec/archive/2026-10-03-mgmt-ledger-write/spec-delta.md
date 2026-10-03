# Spec Delta: mgmt-ledger（后端实现侧）

> 机器可读契约真源在前端 `frontend-scaffold/docs/api/mgmt-ledger.openapi.json`，本文件仅记录后端
> 实现与契约的对应关系，不另存 OpenAPI。

## 新增端点（path key 合并多 method）
- `POST /api/v1/mgmt-ledger/{domain}/rows` — 新增台账行（ADMIN）→ `Result<Long>` 新建行 id
- `PUT /api/v1/mgmt-ledger/{domain}/rows/{rowId}` — 更新台账行（ADMIN）→ `Result<Void>`
- `DELETE /api/v1/mgmt-ledger/{domain}/rows/{rowId}` — 删除台账行（ADMIN）→ `Result<Void>`

## 变更端点
- `GET /api/v1/mgmt-ledger/{domain}` — `MgmtLedgerListResult` 新增 `rowIds: List<Long>`
  （与 `rows` 一一对应，用于前端编辑/删除定位）。

## 新增 DTO（与契约 schema 同名同字段）
- `MgmtLedgerRowWriteRequest`：`{ cells: MgmtLedgerCellWriteDto[] }`
- `MgmtLedgerCellWriteDto`：`{ colIndex: Integer; text: String; type: String }`

## 不变
- `GET /api/v1/mgmt-ledger/{domain}/meta` 及其 `MgmtLedgerMetaDto` 不变。
- 读端点零下行控制、仅登录态语义不变。
