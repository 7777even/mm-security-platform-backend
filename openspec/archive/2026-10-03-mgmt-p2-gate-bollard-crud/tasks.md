# Tasks: 安防设备台账 CRUD——道闸与防恐柱（后端）

## 权限与表结构（Task 1）

- [x] V101 三方言迁移 `V101__security_device_ledger_perm.sql`：登记 `security:gate-write` / `security:bollard-write` 按钮菜单并授权 ADMIN 及岗位角色
- [x] `fac_gate_control` / `fac_bollard` 增加 `version BIGINT DEFAULT 0` 乐观锁列

## DTO（Task 2）

- [x] 新增 `dto/GateControlWriteRequest.java`（`name` `@NotBlank`，无 `status` 字段）
- [x] 新增 `dto/BollardWriteRequest.java`（`name` `@NotBlank`，无 `status` 字段）
- [x] 实体 `FacGateControl` / `FacBollard` 加 `@Version` 字段

## Service（Task 3）

- [x] `SecurityService` 新增 `createGate` / `updateGate` / `deleteGate`，标注 `@RealtimeSync(domain="security.gate-control")`
- [x] 新增 `createBollard` / `updateBollard` / `deleteBollard`，标注 `@RealtimeSync(domain="security.bollard")`

## Controller（Task 4）

- [x] `SecurityController` 新增道闸 POST / PUT / DELETE 与防恐柱 POST / PUT / DELETE（`@RequireAuth` + `@Valid`）

## 契约四同步（Task 5）

- [x] 前端 `docs/api/security.openapi.json` 补 6 个写端点（同 path 多 method 合并）+ 两个 WriteRequest schema
- [x] `npm run gen:api-types` 重产 `src/types/generated/security.ts`
- [x] `node scripts/check-api-contract.mjs --strict` 路由差异 0 / schema 漂移 0

## 测试与回归（Task 6）

- [x] `SecurityControllerTest`：道闸/防恐柱新增返回 Item、必填缺失→B3 400、删除→200、不存在→404
- [x] `SecuritySearchCrudServiceTest`：道闸/防恐柱 CRUD + 写方法广播域断言 + notFound
- [x] `mvn -o test` 相关类全绿（48/48）；jacoco 覆盖率达标

## 收尾（Task 7）

- [ ] 双仓推送（`feature/mgmt-p2-gate-bollard` 已 ff-only 合 `main`）；与前端 mgmt 台账联调（待本 Change 上列项全部完成后归档）
