# Tasks: 通用台账写端点权限粒度化

## 后端
- [x] `MgmtLedgerController` 三写端点 `role=ADMIN` → `perm=mgmt-ledger:write`
- [x] V107 三方言（h2/postgresql/dameng）播种 `fm-mgmt` 目录 + `mgmt-ledger:write` 按钮 + 角色授权
- [x] 顺带修复 V103–V106 误放 stray `postgres/` 目录（移回活动 `postgresql/`，prod 缺 4 个迁移）

## 前端
- [x] `MgmtLedgerView` 新增/编辑/删除按钮加 `v-permission="'mgmt-ledger:write'"`

## 验证
- [x] 后端 `mvn -o compile` 绿；`check-endpoint-authz` 写端点 147 全约束；`check-api-contract --strict` 路由差异 0 / schema 漂移 0
- [x] 前端 `vue-tsc` 绿；`eslint` 0 error
- [x] 归档本 Change 并双仓推送（含前端 `feat(mgmt)` 配套）
