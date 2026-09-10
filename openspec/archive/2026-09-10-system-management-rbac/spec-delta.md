# Spec Delta：2026-09-10-system-management-rbac

> 与 `openspec/specs/<capability>/spec.md` 同构。三段：ADDED / MODIFIED / REMOVED。
> 全部勾选并验收后，**同一次交付内**合入 `openspec/specs/` 并归档本 Change（`changes/README.md` §闭环纪律）。

---

## ADDED Capability: system-management

### Requirement: 系统管理域端点与门禁

系统须提供系统管理域（用户 / 角色 / 菜单权限 / 字典）的查询与变更端点，默认仅 `ADMIN` 可访问；端点返回统一 `Result<T>` 包络，分页返回 `{list,total,page,size}`。

#### Scenario: 非管理员访问系统管理域

- **WHEN** 非 `ADMIN` 角色调用 `GET /api/v1/system/users`
- **THEN** 返回 403，不返回任何用户数据

#### Scenario: 管理员查询用户分页

- **WHEN** `ADMIN` 调用 `GET /api/v1/system/users?page=1&size=10`
- **THEN** 返回 200 + `Result.ok`，`data.list` 每项含 `id/username/realName/roleCode/roleName/status`，**不含 `passwordHash`**

### Requirement: 角色与授权模型

系统须以 `sys_role`（角色）与 `sys_role_menu`（角色-菜单/权限授权）实现可配置 RBAC；`sys_user.role` 引用角色码；角色的菜单/权限授权变更后须立即生效（写时失效缓存），无需等待令牌过期。

#### Scenario: 角色授权变更即时生效

- **WHEN** 管理员通过 `PUT /api/v1/system/roles/{id}/menus` 修改某角色授权
- **THEN** 该角色用户下一次请求的角色权限解析即为新结果（缓存被主动失效）

#### Scenario: 分配角色

- **WHEN** 管理员通过 `PUT /api/v1/system/users/{id}/role` 将某用户角色改为 `SCHEDULER`
- **THEN** 该用户后续请求的 `/auth/me` 返回 `roles` 含 `SCHEDULER`，其可见菜单与权限码随之变化

### Requirement: 自锁与提权防护

系统须在服务端强制以下约束，不得仅靠前端禁用：禁止对自己执行删除 / 停用 / 改角色；禁止使启用状态的 `ADMIN` 用户数归零；内置对象（`built_in=1`）禁删。

#### Scenario: 删除自身账号

- **WHEN** 管理员调用 `DELETE /api/v1/system/users/{自己id}`
- **THEN** 返回 403，账号未被删除

#### Scenario: 移除最后一个管理员

- **WHEN** 请求会使启用状态 `ADMIN` 用户数归零（删除 / 停用 / 改角色最后一名管理员）
- **THEN** 返回 409，请求被拒绝，系统始终保留至少一个启用管理员

#### Scenario: 删除内置角色

- **WHEN** 管理员调用 `DELETE /api/v1/system/roles/{内置角色id}`
- **THEN** 返回 403，内置角色未被删除

### Requirement: 系统管理写操作服务端审计

系统管理域每个写操作成功后，服务端须向 `fac_audit_log` 落一条审计（`module='system'`，`detail_json` 脱敏且不含口令）；审计写入失败不得阻断主流程；审计记录 append-only，不可改删。

#### Scenario: 重置密码不落明文

- **WHEN** 管理员重置某用户密码成功
- **THEN** `fac_audit_log` 存在 `action='system.user.reset-password'` 记录，且 `detail_json` **不含**任何口令明文或哈希

#### Scenario: 审计写入失败不阻断

- **WHEN** 审计落库抛异常
- **THEN** 主操作仍返回成功，仅记录 warn 日志

---

## ADDED Capability: password-lifecycle

### Requirement: 本人修改密码

系统须提供 `POST /api/v1/auth/password`，校验旧密码与新口令复杂度后方可修改；成功后更新 `pwd_updated_at`、清除 `must_change_pwd`。

#### Scenario: 旧密码错误

- **WHEN** 携带错误的旧密码调用 `POST /api/v1/auth/password`
- **THEN** 返回失败，密码未被修改

#### Scenario: 不满足复杂度

- **WHEN** 新口令不满足复杂度策略（长度或字符类别）
- **THEN** 返回失败并说明策略，密码未被修改

### Requirement: 管理员重置密码

系统须提供 `POST /api/v1/system/users/{id}/password/reset`，由服务端生成随机临时密码（不得为固定值），置 `must_change_pwd=1`；响应不返回口令哈希。

#### Scenario: 重置后强制改密

- **WHEN** 管理员重置某用户密码
- **THEN** 该用户下次登录响应 `mustChangePwd=true`，且在完成改密前不得访问业务写端点

### Requirement: 默认账号强制首登改密

默认管理员账号（`ensureAdmin` 种子）须置 `must_change_pwd=1`，首次登录后强制修改密码。

#### Scenario: 默认账号首登

- **WHEN** 以种子账号首次登录
- **THEN** 登录响应标记需改密，未改密前业务路由不可达

---

## MODIFIED Capability: auth-rbac

### Requirement: 角色门禁与越权守门

系统须基于 `sys_role` / `sys_role_menu`（**取代**原 `sys_user.role` 自由字符串 + `sys_menu.allowed_roles` 逗号串）实现 RBAC；端点级门禁支持 `@RequireAuth(role=...)`（单角色名）与 `@RequireAuth(perm=...)`（权限码，经服务端缓存解析）；资源级归属校验沿用 `AuthorizationService.assertSelfOrAdmin`；不可将角色硬编码进业务逻辑。

#### Scenario: 端点角色门禁

- **WHEN** 非 `ADMIN` 角色访问标注 `@RequireAuth(role="ADMIN")` 的端点
- **THEN** 返回 403，不执行业务逻辑

#### Scenario: 端点权限码门禁

- **WHEN** 当前角色不持有标注 `@RequireAuth(perm="system:user:create")` 所要求的权限码
- **THEN** 返回 403，不执行业务逻辑

#### Scenario: 菜单按角色过滤

- **WHEN** 某角色请求 `GET /api/v1/auth/menus`
- **THEN** 返回仅含该角色经 `sys_role_menu` 授权、且 `menu_type ∈ {DIR,MENU}`、`visible=1` 的菜单

### Requirement: 登录身份下发

`GET /api/v1/auth/me` 须下发当前用户的 `username`、`realName`、`roles`、`perms`、`mustChangePwd`；`perms` 为该用户角色经 `sys_role_menu` 聚合去重后的权限码全集，作为前端权限判定的唯一权威来源。

#### Scenario: 令牌内角色与库中角色一致

- **WHEN** 用户以 `role=X` 登录后刷新令牌
- **THEN** 换发的新 access 令牌角色仍为库中真实角色 `X`，**不得**被硬编码为 `ADMIN`

#### Scenario: 权限码下发

- **WHEN** 携带有效令牌调用 `GET /api/v1/auth/me`
- **THEN** 返回的 `perms` 与后端判定所用权限码一致（同一解析来源）

---

## MODIFIED Capability: rbac-permission（前端）

### Requirement: 权限来源后端化

前端权限码须以 `GET /auth/me` 的 `perms` 为唯一来源，**退役**硬编码于 `stores/auth.ts#ROLE_PERMS` 的权限清单；角色集合由后端 `sys_role` 驱动的可配置列表提供。

#### Scenario: 权限变更生效

- **WHEN** 后端调整某角色授权后用户重新登录
- **THEN** 前端 `hasPerm` 结果与新授权一致，无残留硬编码权限

#### Scenario: 启动时序

- **WHEN** 应用启动装配路由
- **THEN** 在路由守卫判定 `meta.perm` 之前已完成 `/auth/me` 拉取，避免权限空集导致的误跳 404

---

## REMOVED Capability: （无）

本 Change 不移除任何既有 capability。`sys_menu.allowed_roles` 列**降级为只读兼容列**（数据迁移至 `sys_role_menu` 后不再写入），其删除留待后续版本，不在本 Change 移除。
