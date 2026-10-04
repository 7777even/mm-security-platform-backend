# 设计：通用台账写端点权限粒度化

## 权限码
`mgmt-ledger:write` —— 单一权限码覆盖 25 个通用台账域的写能力（与后端 `@RealtimeSync` 域 `mgmt-ledger` 对齐）。

## 授权角色（V107 种子）
`ADMIN` / `COMMANDER` / `SCHEDULER` / `TEAM_LEADER`（指挥与值班调度等管理岗）。
不含 `INNER_OPER` / `OUTER_OPER`（装置内外操）——通用台账为后台管理控制台，不对一线操作员开放。
后续如需放开，到「角色管理」勾选 `mgmt-ledger:write` 即可，无需改代码。

## 菜单树
新增隐藏目录 `fm-mgmt`（后台管理台账，`parent_id=0`，`visible=0`）作为容器；隐藏按钮 `fm-mgmt-ledger-write` 挂其下，
`perm_code=mgmt-ledger:write`，`visible=0`（仅作 RBAC 授权载体，不进导航）。

## 前后端一致性
后端改 `perm` 后，前端写按钮同步以 `v-permission` 门控，消除「非 ADMIN 只见按钮、调用却被 403」的 UX 不一致。

## 附带修复
V103–V106 此前被误放进 stray 的 `postgres/` 目录，而活动 prod profile 用的是 `postgresql/`，
导致 **prod 缺 4 个迁移**（hazard / special-operation / video·comm·device 写 + 七域权限种子）。
本批将其 `git mv` 回 `postgresql/`，修正 prod RBAC 链路后再落 V107。
