# Spec Delta: 通用台账列表查询下推

## 变更
- `MgmtLedgerService.list()` 内部实现由「全量内存过滤+分页」改为「DB 匹配命中行主键 + DB 分页」，
  对外行为（路径 `/api/v1/mgmt-ledger/{domain}`、参数 `page/size/keyword/f_<列>`、响应结构）**不变**。
- `MgmtLedgerCellMapper` 新增两个只读查询方法（无新建表/迁移）。

## 兼容性
- 前端 `services/mgmtLedger.ts` 调用方式不变，无需改契约、无需重新生成类型。
- 三方言（h2/postgresql/dameng）均为标准 SQL；PaginationInnerInterceptor 已配置。

## 待复核
- PG / DM 真库未在本环境运行（无 Docker），relational-division 与 `LIKE ESCAPE` 需在真库确认。
