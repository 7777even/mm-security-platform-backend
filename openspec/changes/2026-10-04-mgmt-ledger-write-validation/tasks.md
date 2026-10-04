# Tasks: 通用台账写端点单元格结构性校验

## 后端
- [x] MgmtLedgerService 新增 validateCells（非空/colIndex 越界/重复 → PARAM_INVALID）
- [x] createRow/updateRow 落库前调用 validateCells
- [x] MgmtLedgerServiceTest 扩 4 例写校验（空/越界/重复/合法落库），共 10 例全绿

## 验证
- [x] 后端 mvn -o test -Dtest=MgmtLedgerServiceTest 10/10 绿、jacoco 达标
- [x] 前端契约不受影响（写接口签名未变），check-api-contract 路由差异维持基线 0
- [ ] 归档本 Change 并推送 origin main
