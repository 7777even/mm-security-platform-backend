# 契约变更说明

新增对外接口（后端实现，前端契约真源 `frontend-scaffold/docs/api/mgmt-ledger.openapi.json` 同步）：

## 新增
- `GET /api/v1/mgmt-ledger/{domain}` → `MgmtLedgerListResult`
- `GET /api/v1/mgmt-ledger/{domain}/meta` → `MgmtLedgerMetaDto`

## Schema
- `MgmtLedgerMetaDto{title:string, columns:string[], filters:MgmtLedgerFilterDto[]}`
- `MgmtLedgerFilterDto{column:string, options:string[]}`
- `MgmtLedgerCellDto{text:string|null, type:('ok'|'warn'|'bad'|null)}`
- `MgmtLedgerListResult{columns:string[], filters:MgmtLedgerFilterDto[], rows:MgmtLedgerCellDto[][], total:int, page:int, size:int}`

## 四同步
- openspec（本 Change）→ 前端契约 `mgmt-ledger.openapi.json`（schema 名与后端 DTO 类名逐一对应）→ 后端实现（controller/service/mapper/entity/dto）→ 前端 `gen:api-types` 重新生成 `mgmt-ledger.ts`。
- 契约校验 `scripts/check-api-contract.mjs --strict`：schema 漂移 0。
