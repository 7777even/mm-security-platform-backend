# Design

## MgmtLedgerService
- 新增 `validateCells(List<String> columns, List<MgmtLedgerCellWriteDto> cells)`：
  空数组→PARAM_INVALID；colIndex==null→PARAM_INVALID；越界→PARAM_INVALID；重复→PARAM_INVALID。
- `createRow` / `updateRow` 在 `insertCells` 前调用 `validateCells(columns, req.getCells())`。
- `insertCells` 保留 `if (cells == null) return;` 防御（validateCells 已保证非空）。
