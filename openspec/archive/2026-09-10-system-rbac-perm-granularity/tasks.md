# Tasks — 系统管理域鉴权粒度细化

> 对应 `proposal.md`。纯注解细化 + 单测，无 DB / 契约变更。

## 实施任务

- [ ] 1. `SystemUserController`：类级保留 `role="ADMIN"`，8 个方法逐个加 `perm=system:user:{view,create,edit,delete,reset-pwd,assign-role}`（见 proposal §3 映射表）。
- [ ] 2. `SystemRoleController`：类级保留 `role="ADMIN"`，8 个方法加 `perm=system:role:{view,create,edit,delete,grant}`。
- [ ] 3. `SystemMenuController`：类级保留 `role="ADMIN"`，5 个方法加 `perm=system:menu:{view,create,edit,delete}`。
- [ ] 4. `SystemDictController`：类级保留 `role="ADMIN"`，8 个方法加 `perm=system:dict:{view,create,edit,delete}`；`options()` 方法级 `@RequireAuth`（仅登录）不动。
- [ ] 5. 补单测：ADMIN（持全部 `system:*` perm）访问系统管理端点全过；某无 `system:*` 授权的角色访问 → 403。
- [ ] 6. 门禁：跑 `mvn test` + `check-api-contract.mjs --strict` + `check-openspec-hygiene.mjs`；冒烟 `smoke_system_rbac.py` 仍 54/54。

## 收尾任务

- [ ] 7. 后端 `docs/system-facts.md` 补充「鉴权粒度已细化至 perm」说明（可选，保持事实基线新鲜）。
- [ ] 8. 归档 Change；按 scope 提交 `feat(security): 系统管理域端点鉴权粒度细化至 perm` 并推送 `main`。

## 不在范围（已登记，建议独立 Change）

- `data_scope` 行级防区过滤（ABAC）——独立大块，当前多角色未实际启用，严重超前。
- 账号登录失败锁定——设计已决议不做（耦合登录链路、当前无公网暴露收益）。
