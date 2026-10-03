# Proposal: 通用台账 25 域行级写能力（后端实现）

## Why

后台管理端 25 个通用台账域（alarm-config / drill-mgmt / ef-tank / enterprise-basic …）此前后端
`MgmtLedgerController` **仅 GET**，数据由 V51/V52/V53 迁移种子化进 `mgmt_ledger_*` 表。菜单大量叶子
标了 `action:'新增X'`，但 `MgmtLedgerView` 只能看不能改——写能力是「承诺未兑现」。

本次补齐后端行级写端点（新增/编辑/删除），与前端契约四同步（`frontend-scaffold` 同号 Change
`2026-10-03-mgmt-ledger-write` 为契约真源侧），让 25 域从「只读台账」升级为「可维护台账」。

## What Changes

### Service（`MgmtLedgerService`）
- 新增 `createRow(domain, req)` / `updateRow(domain, rowId, req)` / `deleteRow(domain, rowId)`，均 `@Transactional`。
- 行与单元格在同一事务内维护；主键与排序号由 `LedgerIdSupport.nextId` / `nextSortNo` 显式分配（max+1），
  规避 Flyway 种子显式插 id 导致的自增序列滞后、新增撞主键（409「数据冲突」）。
- `list()` 重构：构造 `RowView{Long id, List<MgmtLedgerCellDto> cells}`，筛选/分页后在**同一分页切片**
  上同时抽取 `rows` 与 `rowIds`，保证 `rowIds[i]` 恒对应 `rows[i]`（修复旧实现 rowIds 来自全量行的错位）。

### Controller（`MgmtLedgerController`）
- 新增 3 个写端点：`POST /{domain}/rows`、`PUT /{domain}/rows/{rowId}`、`DELETE /{domain}/rows/{rowId}`，
  均 `@RequireAuth(role = "ADMIN")`（mgmt 控制台管理员维护，与 FormRecord / EmergencyPlan 同口径）。
- `POST` 返回 `Result<Long>`（新建行 id），供前端免重拉回填。

### DTO
- `MgmtLedgerRowWriteRequest`：`{ List<MgmtLedgerCellWriteDto> cells }`。
- `MgmtLedgerCellWriteDto`：`{ Integer colIndex; String text; String type }`（type 取值 ok/warn/bad，缺省普通文本）。
- `MgmtLedgerListResult` 新增 `List<Long> rowIds`。

### 异常处理
- 行不存在 / domain 不匹配：`BusinessException(NOT_FOUND)` → B3 `code=404`。
- 唯一键/外键冲突：`DataIntegrityViolationException` 由 `GlobalExceptionHandler` 自动收敛为 B3
  `HTTP 409 code=409`，无需手写 catch。

## Impact

- 影响文件：`MgmtLedgerService.java`、`MgmtLedgerController.java`、2 个新 DTO、`MgmtLedgerListResult.java`。
- 风险：写端点需 ADMIN 角色；若 mgmt 操作账号非 ADMIN 将无法写入（与既有 mgmt 写端点一致）。
- 零下行控制：台账维护不涉及任何设备下行（红线无关）。
- 未含实时订阅（更新后前端需手动刷新），实时化留待 P2。
