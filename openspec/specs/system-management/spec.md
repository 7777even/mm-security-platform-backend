# system-management Specification

## Purpose

系统管理域能力：用户 / 角色 / 菜单权限 / 数据字典的查询与变更端点，以及服务端强制的自锁与提权防护。数据模型（`sys_role` / `sys_role_menu` / `sys_dict_type` / `sys_dict_item`）与权限码口径见 `auth-rbac` capability；口令生命周期见 `password-lifecycle`。设计依据见 `openspec/changes/2026-09-10-system-management-rbac/design.md`（已归档）。

## Requirements

### Requirement: 系统管理域端点与门禁

系统须提供系统管理域（用户 / 角色 / 菜单权限 / 字典）的查询与变更端点，默认仅 `ADMIN` 可访问；端点返回统一 `Result<T>` 包络，分页返回 `{list,total,page,size}`。唯一例外为 `GET /api/v1/system/dicts/{dictCode}`（业务只读，任何已登录用户可用）。

#### Scenario: 非管理员访问系统管理域

- **WHEN** 非 `ADMIN` 角色调用 `GET /api/v1/system/users`
- **THEN** 返回 403，不返回任何用户数据

#### Scenario: 管理员查询用户分页

- **WHEN** `ADMIN` 调用 `GET /api/v1/system/users?page=1&size=10`
- **THEN** 返回 200 + `Result.ok`，`data.list` 每项含 `id/username/realName/roleCode/roleName/status`，**不含 `passwordHash`**

#### Scenario: 字典业务读取不受 ADMIN 限制

- **WHEN** 非 `ADMIN` 的已登录用户调用 `GET /api/v1/system/dicts/{dictCode}`
- **THEN** 返回该字典的启用项列表（按 `sortOrder` 排序）

### Requirement: 角色与授权模型

系统须以 `sys_role`（角色）与 `sys_role_menu`（角色-菜单/权限授权）实现可配置 RBAC；`sys_user.role` 引用角色标识；角色的授权变更须立即生效（写时失效缓存），无需等待令牌过期。授权保存为**整表覆盖**语义（空数组表示回收全部授权）。

#### Scenario: 角色授权变更即时生效

- **WHEN** 管理员通过 `PUT /api/v1/system/roles/{id}/menus` 修改某角色授权
- **THEN** 该角色用户下一次请求的权限解析即为新结果（缓存被主动失效）

#### Scenario: 分配角色

- **WHEN** 管理员通过 `PUT /api/v1/system/users/{id}/role` 将某用户角色改为 `SCHEDULER`
- **THEN** 该用户后续请求的 `/auth/me` 返回 `roles` 含 `SCHEDULER`，其可见菜单与权限码随之变化

#### Scenario: 不可清空 ADMIN 授权

- **WHEN** 管理员对 `ADMIN` 角色提交空的授权集合
- **THEN** 返回 409，授权未被清空（避免立即造成管理员无任何权限）

### Requirement: 自锁与提权防护

系统须在服务端强制以下约束，不得仅靠前端禁用：禁止对自己执行删除 / 停用 / 改角色；禁止使启用状态的 `ADMIN` 用户数归零；内置对象（`sys_role.built_in=1` / `sys_dict_type.built_in=1`）禁删。

#### Scenario: 删除自身账号

- **WHEN** 管理员调用 `DELETE /api/v1/system/users/{自己id}`
- **THEN** 返回 403，账号未被删除

#### Scenario: 移除最后一个管理员

- **WHEN** 请求会使启用状态 `ADMIN` 用户数归零（删除 / 停用 / 改角色最后一名管理员）
- **THEN** 返回 409，请求被拒绝，系统始终保留至少一个启用管理员

#### Scenario: 删除内置角色

- **WHEN** 管理员调用 `DELETE /api/v1/system/roles/{内置角色id}`
- **THEN** 返回 403，内置角色未被删除

### Requirement: 引用完整性保护

被引用的对象不得被删除：仍被用户引用的角色、仍有字典项的字典类型、仍有子节点或已被角色授权的菜单节点，删除一律返回 409 并提示先解绑 / 先删下级。

#### Scenario: 删除被用户引用的角色

- **WHEN** 管理员删除某仍有用户的角色
- **THEN** 返回 409，角色未被删除

#### Scenario: 删除仍有子节点的菜单节点

- **WHEN** 管理员删除某仍有子节点的菜单节点
- **THEN** 返回 409，节点未被删除

### Requirement: 标识唯一性与不回收

`sys_user.username`、`sys_role.role_code`、`sys_dict_type.dict_code` 须唯一。由于删除为逻辑删除（行仍物理存在），唯一性判定**必须包含已删行**，否则会出现「预检查放行 → 数据库唯一键拒绝」的笼统失败。标识**不回收**是有意设计（回收会让新旧记录在审计上无法区分）。

#### Scenario: 新建与已删账号同名的用户

- **WHEN** 管理员新增用户名与某历史已删除账号相同的用户
- **THEN** 返回 409 且提示「用户名不可复用（含历史已删除账号）」，而非笼统的数据冲突

### Requirement: 系统管理写操作服务端审计

系统管理域每个写操作成功后，服务端须向 `fac_audit_log` 落一条审计（`module='system'`，`detail_json` 脱敏且不含口令明文或哈希）；审计写入失败不得阻断主流程；审计记录 append-only，不可改删。

#### Scenario: 重置密码不落明文

- **WHEN** 管理员重置某用户密码成功
- **THEN** `fac_audit_log` 存在 `action='system.user.reset-password'` 记录，且 `detail_json` **不含**任何口令明文或哈希

#### Scenario: 审计写入失败不阻断

- **WHEN** 审计落库抛异常
- **THEN** 主操作仍返回成功，仅记录 warn 日志
