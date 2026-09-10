# 任务清单：2026-09-10-system-management-rbac

> **⚠️ 草案 · 未启用**。本文件为 L4 设计评审的**工作量拆解预览**，**不是**已确认的任务真源。
> 依 `changes/README.md` + `AGENTS.md §7.1`：须先通过 `proposal.md`「人工确认关卡」全部勾选，本清单方可启用并回填勾选框。
> 任务粒度 ≤2h/项；状态**只回填勾选框**，不在此处写进度文字。

---

## A. 数据库与迁移（L4 · 结构变更）

- [x] A1 编写 `V32__system_rbac.sql`（h2）：`sys_role` / `sys_role_menu` / `sys_dict_type` / `sys_dict_item` 四表 + 索引
- [x] A2 编写 `V33__system_menu_user_extend.sql`（h2）：`sys_menu` 加 `menu_type`/`perm_code`/`visible`，`sys_user` 加 `pwd_updated_at`/`must_change_pwd`
- [x] A3 编写 `V34__system_rbac_seed.sql`（h2）：6 条角色种子 + 5 条 `fm-*` 菜单置 `DIR` 并补 `perm_code`
- [x] A4 编写 `sys_role_menu` 迁移脚本：把 `sys_menu.allowed_roles` 展开为授权行（`USER` 不存在则跳过并告警）
- [x] A5 同步 `postgresql/` 三方言脚本（A1–A3 对应）
- [x] A6 同步 `dameng/` 三方言脚本（去 `IF NOT EXISTS`，Oracle 兼容写法）
- [x] A7 h2 空库实跑 Flyway 验证（`V1→V34` 直达最新结构）+ 复核 PG/DM 脚本

## B. 后端数据层

- [x] B1 新增实体 `SysRole` / `SysRoleMenu` / `SysDictType` / `SysDictItem`
- [x] B2 `SysMenu` 扩字段（`menuType`/`permCode`/`visible`）、`SysUser` 扩字段（`pwdUpdatedAt`/`mustChangePwd`）；确认 `allowedRoles` 降级只读
- [x] B3 新增四个 `BaseMapper`（`SysRoleMapper` 等）
- [x] B4 `ensureAdmin()` 扩展：幂等确保 `ADMIN` 角色存在 + `admin→ADMIN` 绑定 + ADMIN 全量授权 + 置 `must_change_pwd=1`

## C. 后端鉴权链路（L4 · 门禁区）

- [x] C1 `RequireAuth` 增 `perm` 属性（不改 `role`/`value` 语义）
- [x] C2 新增 `RoleAuthorityService`（Caffeine `role_code→Set<perm_code>`，TTL 5min + `reloadRolePerms()`）
- [x] C3 `RequireAuthInterceptor` 增 `perm` 判定分支（401/403 语义与现有对齐，补单测）
- [x] C4 `AuthService.menus()` 数据源由 `allowed_roles` 切到 `sys_role_menu`（**返回结构不变**）
- [x] C5 **修复** `AuthService.refresh()` 角色硬编码 `"ADMIN"` → 回归库中真实角色 + 单测
- [x] C6 `AuthService.me()` 由 `Map` 改强类型 `MeResult`（含 `roles`/`perms`/`mustChangePwd`）

## D. 后端业务层与端点

- [x] D1 `SystemUserService` + `SystemUserController`：列表/详情/新增/改/逻辑删/启停用/改角色
- [x] D2 自锁与提权防护：禁操作自己、最后管理员保护、内置用户禁删（事务内核验）
- [x] D3 `SystemRoleService` + `SystemRoleController`：角色 CRUD + 启停用 + `GET/PUT /roles/{id}/menus` 授权；被引用禁删
- [x] D4 `SystemMenuService` + `SystemMenuController`：菜单树 CRUD + `GET /system/permissions`（权限码聚合）
- [x] D5 `SystemDictService` + `SystemDictController`：字典类型/项 CRUD + `GET /system/dicts/{dictCode}`（业务只读，缓存）
- [x] D6 `PasswordPolicy`（长度 + 字符类别 + 不含用户名 + 不与旧密码同）+ 配置项 `app.password.*`
- [x] D7 `POST /auth/password`（验旧密码 + 策略 + 清 `must_change_pwd`）
- [x] D8 `PUT /auth/profile`（本人资料，禁改自身角色/状态）
- [x] D9 `POST /system/users/{id}/password/reset`（随机临时密码 + `must_change_pwd=1`）
- [x] D10 `SystemAuditHelper` + 各 Service 写操作接入审计（`module='system'`，脱敏、不含口令）
- [x] D11 出口脱敏：用户 DTO `realName` 走 `@Masked`；确认无 `passwordHash` 泄漏
- [x] D12 用户/菜单写操作触发现有缓存失效（`IdNameCacheService.evict` / `reloadMenus` / `reloadRolePerms`）

## E. 契约（四同步）

- [x] E1 新增 `frontend-scaffold/docs/api/system.openapi.json`（users/roles/menus/dicts 四组，四条铁律）
- [x] E2 修改 `frontend-scaffold/docs/api/auth.openapi.json`（`/auth/me` 增字段、增 `/auth/password`、`/auth/profile`）
- [x] E3 `node scripts/check-api-contract.mjs --strict` 路由 + schema 双层级零差异
- [x] E4 `node scripts/validate-api-contracts.mjs` 通过（域数 27→28）

## F. 后端测试

- [x] F1 `SystemUserControllerTest` / `SystemRoleControllerTest` / `SystemMenuControllerTest` / `SystemDictControllerTest`（standalone MockMvc）
- [x] F2 `SystemUserServiceTest`：**最后管理员保护** / **自锁防护** / **内置禁删** 三类负例
- [x] F3 `PasswordPolicyTest` + `/auth/password` 与 reset 的服务层单测
- [x] F4 `RoleAuthorityServiceTest`（缓存 + 写时失效）+ `RequireAuthPermTest`（perm 401/403）
- [x] F5 `AuthServiceTest` 补 `refresh` 角色回归用例 + `me` 的 `roles/perms` 断言
- [x] F6 `mvn test` 全绿 + jacoco 门禁达标（基线 361 → 预计 +50±）

## G. 前端

- [x] G1 `services/system.ts`：用户/角色/菜单/字典 REST 封装（`request()` + 类型）
- [x] G2 `services/auth.ts` 扩 `fetchMe` / `changePassword` / `updateProfile`；`npm run gen:api-types` 重生成
- [x] G3 `stores/auth.ts`：`perms` 改由 `/auth/me` 填充，退役 `ROLE_PERMS`，`RoleId` 放宽为 `string`
- [x] G4 启动时序：路由解析前完成 `/auth/me`（perms）+ `/auth/menus`
- [x] G5 `views/system/users.vue` 落地（列表/筛选/新增编辑/角色下拉/启停用/重置密码/删除）
- [x] G6 新增 `views/system/roles.vue`（角色 CRUD + 授权树）
- [x] G7 新增 `views/system/menus.vue`（菜单权限树维护）
- [x] G8 新增 `views/system/dict.vue`（字典类型 + 字典项）
- [x] G9 `router/index.ts` 增 3 条 `SECONDARY_ROUTES`（`meta.perm` / adminOnly）
- [x] G10 强制改密流程：`mustChangePwd=true` 时拦截跳改密页
- [x] G11 前端测试：`stores/auth.spec.ts` 更新 + `services/system.spec.ts` + 组件测试
- [x] G12 `vitest run` + `vue-tsc -p tsconfig.app.json --noEmit` + `lint` 全绿

## H. 收尾与文档

- [x] H1 端到端冒烟（Python utf-8 脚本）：admin 登录 → 建角色 → 建用户 → 赋角色 → 重置密码 → 新用户登录验证边界
- [x] H2 文档同步：`docs/architecture/auth-design.md`（RBAC 模型）、`id-name-cache.md`（新增失效入口）、`system-facts.md`（前后端）、`password-security.md`
- [x] H3 顺带清理 §15：`AGENTS.md §6.3.3` 白名单与实际对齐
- [x] H4 `spec-delta.md` 合入 `openspec/specs/`（后端 `auth-rbac`/`system-management`/`password-lifecycle`；前端 `rbac-permission`）
- [x] H5 `git mv` 本 Change → `openspec/archive/2026-09-10-system-management-rbac/`；前端镜像 Change 同办
- [x] H6 双库按 scope 拆分提交（`db` / `security` / `auth` / `system` / `docs`）并推送；`check-openspec-hygiene.mjs` 通过
