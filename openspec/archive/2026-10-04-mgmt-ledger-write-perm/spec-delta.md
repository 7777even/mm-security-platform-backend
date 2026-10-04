# Spec Delta: 通用台账写端点权限粒度化

## 改动摘要
- `MgmtLedgerController.createRow` / `updateRow` / `deleteRow`：`@RequireAuth(role="ADMIN")` → `@RequireAuth(perm="mgmt-ledger:write")`
- 新增 V107 迁移（三方言）：`sys_menu` 播种 `fm-mgmt`（DIR, 隐藏）+ `fm-mgmt-ledger-write`（BUTTON, 隐藏, `perm_code=mgmt-ledger:write`）；`sys_role_menu` 授予 `ADMIN`/`COMMANDER`/`SCHEDULER`/`TEAM_LEADER`
- `MgmtLedgerView.vue`：新增/编辑/删除按钮加 `v-permission="'mgmt-ledger:write'"`

## 兼容性
- 写接口路径与签名不变，契约无变化。
- 权限语义：原仅 ADMIN 可写；现 ADMIN/COMMANDER/SCHEDULER/TEAM_LEADER 可写（经 RBAC 角色授权），INNER/OUTER 不可写，与原「仅 ADMIN」相比适度放开至管理岗。
- V103–V106 由 stray `postgres/` 移回活动 `postgresql/`，prod 迁移链路补齐（无内容改动）。
