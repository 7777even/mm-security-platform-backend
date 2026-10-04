# Tasks: 通用台账列表查询下推 DB 分页与筛选

## 后端
- [x] MgmtLedgerCellMapper 新增 selectRowIdsMatchingAllFilters / selectRowIdsByKeyword（标准 SQL，三方言通用）
- [x] MgmtLedgerService.list() 下推：列筛选+关键字在 DB 匹配行主键，再对命中行 selectPage，仅取本页单元格
- [x] 删除无用 matchKeyword/matchFilters；补齐 escapeLike/intersect 辅助
- [x] 新增 MgmtLedgerServiceTest（6 例：无筛选分页 / 仅关键字 / 仅列筛选 / 关键字∩筛选 / 命空中路 / 占位值忽略）

## 验证
- [x] 后端 mvn -o test -Dtest=MgmtLedgerServiceTest 6/6 绿、jacoco 达标
- [x] 前端契约不受影响（list 接口签名未变），check-api-contract 路由差异维持基线 0
- [ ] 归档本 Change 并推送 origin main
