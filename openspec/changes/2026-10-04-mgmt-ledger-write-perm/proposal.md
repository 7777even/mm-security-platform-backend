# 提案：通用台账写端点权限粒度化

## 背景
`MgmtLedgerController` 的 `createRow` / `updateRow` / `deleteRow` 三个写端点当前统一用
`@RequireAuth(role = "ADMIN")` 硬编码角色约束。这与系统其它写端点（V100–V106 已落地的
security / fire / video / communication / device / hazard / special-operation 等）改用
正式权限码（`perm`）的做法不一致，且无法按角色精细授权——要么全 ADMIN，要么无。

## 目标
将三个写端点改为 `@RequireAuth(perm = "mgmt-ledger:write")`，并播种该权限码到 RBAC
（`sys_menu` + `sys_role_menu`），使通用台账写能力可经「角色管理」按角色授予，而非仅对 ADMIN 开放。

## 范围
- 后端：`MgmtLedgerController` 三端点注解置换；V107 三方言权限种子。
- 前端：`apps/mgmt/views/MgmtLedgerView.vue` 新增/编辑/删除按钮加 `v-permission="'mgmt-ledger:write'"`。
- 契约：写接口签名不变，无契约改动。
