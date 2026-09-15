# 设计

## 数据模型
- `mgmt_ledger_meta(domain PK, title, columns_json, filters_json)`：每域一行，列名数组 + 筛选项（column/options）。
- `mgmt_ledger_row(id, domain, sort_order)`：每域多行。
- `mgmt_ledger_cell(id, row_id, col_index, text, type)`：单元格，`type∈{ok,warn,bad,null}` 供前端着色。

## 端点
- `GET /api/v1/mgmt-ledger/{domain}/meta` → `MgmtLedgerMetaDto{title,columns[],filters[]}`。
- `GET /api/v1/mgmt-ledger/{domain}?page&size&keyword&f_<col>=<val>` → `MgmtLedgerListResult{columns[],filters[],rows[][cell],total,page,size}`。
- 未知 domain → 404（`业务异常：台账不存在`）；筛选仅对 `f_` 前缀参数生效，空/`全部` 忽略。

## 技术约束
- 统一 `Result<T>` 包络，code=0 成功。
- 分页基于 MyBatis-Plus；种子数据三方言 CREATE/INSERT 通用。
- 鉴权：JwtFilter 拦截，需 `Authorization: Bearer`。
- 范围：只服务只读展示，不写审计、不落任何下行控制。
