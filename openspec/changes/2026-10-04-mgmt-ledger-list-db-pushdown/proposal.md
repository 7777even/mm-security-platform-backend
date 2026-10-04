# Proposal: 通用台账列表查询下推 DB 分页与筛选

## 背景
`MgmtLedgerService.list()` 当前先 `rowMapper.selectList` 把**该 domain 全部行**拉到内存，
再在 Java 里做 keyword 模糊匹配 + 列筛选 + `subList` 分页。台账行数上千时，每次列表请求都是
全表扫描 + 全单元格 join，O(N) 内存与 CPU，且 `total` 也来自内存计数。

## 目标
把分页与筛选下推到数据库：
- 列筛选用 relational-division（`GROUP BY row_id HAVING COUNT(DISTINCT col_key) = 条件数`）在 `mgmt_ledger_cell` 上匹配行主键；
- 关键字用 `LIKE ... ESCAPE '\'` 匹配单元格文本；
- 两者在 Java 求交集得到命中行主键集合，再对命中行做 `selectPage`（DB 分页），仅取本页单元格组装。

## 非目标
- 前端契约不变（`/api/v1/mgmt-ledger/{domain}` 路径/参数/响应结构均未改），无需改契约或生成类型。
- 不引入方言特有函数，SQL 为 H2/PG/DM 通用标准 SQL。

## 风险
- relational-division / `LIKE ESCAPE` 为标准 SQL，但 PG/DM 真库未在本环境跑过（无 Docker），需在真库复核。
- `selectPage` 依赖已配置的 `PaginationInnerInterceptor`（已确认存在于 `MybatisPlusConfig`）。
