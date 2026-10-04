# Design: 下推实现

## 新增 Mapper 查询（MgmtLedgerCellMapper）
- `selectRowIdsMatchingAllFilters(domain, clauses, filterCount)`：
  `JOIN mgmt_ledger_row` 域限定 + `(col_key=? AND cell_text=?) OR ...` + `GROUP BY row_id HAVING COUNT(DISTINCT col_key)=filterCount`。
- `selectRowIdsByKeyword(domain, keyword)`：
  `JOIN ... WHERE cell_text LIKE #{keyword} ESCAPE '\'`（调用方负责 `%` 包裹与转义）。

## 重写 MgmtLedgerService.list()
1. `meta(domain)` 校验 domain 并取列定义（结果列/筛选定义不变）。
2. 列筛选非空 → 调 `selectRowIdsMatchingAllFilters`（占位值「全部/全部中队」忽略）。
3. 关键字非空 → 调 `selectRowIdsByKeyword`（pattern=`%`+escapeLike(kw)+`%`）。
4. 两者交集（`intersect`）= `matched`；`matched` 为空 → 直接返回空页（短路，不再查行）。
5. `matched==null`（无筛选无关键字）→ `selectPage` 直接按 domain 分页；否则 `in(id, matched)` 分页。
6. 仅取本页 `rowIds` 的单元格组装 RowView；`total` = `matched==null ? page.getTotal() : matched.size()`。

## 删除
- 原 `matchKeyword` / `matchFilters` 私有方法（内存过滤）已无用，移除。

## 转义
- `escapeLike`：`\` `%` `_` 前加 `\`，配合 `ESCAPE '\'` 防误当通配符。
