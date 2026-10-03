# Design: 通用台账 25 域行级写能力（后端）

## 统一台账模型
三张表构成一套通用台账能力，按 `domain`（菜单叶子 path 去前导 `/`）分域：
- `mgmt_ledger_meta`：列定义（columns_json）+ 筛选（filter_json）+ 标题，每域一行。
- `mgmt_ledger_row`：行（domain + row_no + sort_no）。
- `mgmt_ledger_cell`：单元格（row_id + col_index + col_key + cell_text + cell_type）。

写操作只动 `row` + `cell`，不动 `meta`（列结构由种子迁移定义，前端列定义来自 meta，不随写变）。

## 写端点事务边界
- **新增** `createRow`：分配 `row.id` / `row.row_no` / `row.sort_no`（均 `LedgerIdSupport` max+1）
  → `rowMapper.insert` → 按 `cells` 批量 `cellMapper.insert`（每个 cell 也走 `LedgerIdSupport.nextId` 分配主键）。
- **更新** `updateRow`：按 `rowId` 定位（须 `domain` 匹配，否则 `BusinessException(NOT_FOUND)`）
  → 删除该行旧 cell → 重写 cell。行本身不动（保持排序稳定）。返回 void。
- **删除** `deleteRow`：按 `rowId` 定位校验 → 删 cell → 删 row。

## 主键分配（避坑）
Flyway 种子（V51/V52/V53）显式 `INSERT (id,...) VALUES (1,...)` 后，H2/PG/DM 自增序列不推进；
若改用 `@GeneratedValue` 自增，新增会从 id=1 撞主键 409「数据冲突」。故所有写路径统一用
`LedgerIdSupport.nextId(mapper, idRef, getter)` / `nextSortNo(...)` 显式 max+1，三方言一致、零迁移。
（已中招先例：`fac_form_record` V66、`fac_rescue_*` V19/V62。）

## rowIds 对齐
旧 `list()` 的 `rowIds` 来自全量行，与「筛选/分页后显示的 rows」不对齐，前端无法定位。
改为构造 `RowView{id, cells}`，筛选/分页后在**同一分页切片**上同时抽取 `rows` 与 `rowIds`，
保证 `rowIds[i]` 恒对应 `rows[i]`。

## 鉴权与异常
- 写端点 `@RequireAuth(role = "ADMIN")`，与既有 mgmt 写端点（FormRecord.update/delete、EmergencyPlan.*）同口径。
- 读端点维持仅登录态（后台管理端内部使用）。
- 唯一键/外键冲突：`DataIntegrityViolationException` 由 `GlobalExceptionHandler` 自动收敛 B3 `HTTP 409 code=409`。

## 契约真源
机器可读契约真源在前端 `docs/api/mgmt-ledger.openapi.json`（**后端不复制第二份 OpenAPI**）。
守门：`node scripts/check-api-contract.mjs --strict` 比对「Controller (method, path)」与「具名 DTO 字段 + 类型族」，
目标路由差异 0 / schema 漂移 0（既存 17 处 fire-facility 漂移为技术债，非本次引入）。
