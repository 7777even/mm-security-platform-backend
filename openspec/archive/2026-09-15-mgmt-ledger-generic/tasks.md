# 任务清单

- [x] 设计 `mgmt_ledger_meta/row/cell` 三表结构（Flyway V51，h2/postgresql/dameng 三方言）
- [x] 编写 V51 种子数据（18 域 meta/row/cell，含 enterprise-basic 键值台账化）
- [x] 实现 `MgmtLedgerController/Service/Mapper/Entity/Dto`（12 文件）
- [x] 实现只读端点族 `/mgmt-ledger/{domain}` 与 `/meta`，未知域返回 404
- [x] 后端 `mvn compile` 通过；dev(8787) 启动 V51 迁移成功
- [x] 契约校验 `scripts/check-api-contract.mjs --strict` schema 漂移 0
- [x] Python 真后端冒烟：18 域全部返回正确种子数据（含列筛选/关键字/404 路径）
- [x] 前端契约 `mgmt-ledger.openapi.json` schema 名与后端 DTO 对齐
- [x] 归档本 openspec Change 至 archive/2026-09-15-mgmt-ledger-generic
