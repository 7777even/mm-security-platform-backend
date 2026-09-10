# 变更提案：系统管理域 + RBAC 模型升级

> **分级：L4**（触及 `security/` 门禁区、RBAC 模型、数据库结构；参见 `AGENTS.md §6.2` / `§8`、`docs/architecture/auth-design.md §1`）。
> **状态：`approved`（2026-09-10 人工确认，全部按推荐结论采纳）— 已开工。** 本文与 `design.md` 为设计评审输入；`tasks.md` 已启用为任务真源（`§7.1`）。
> 适用端：跨库（后端为主，前端镜像 Change 待确认后建）。

## Why

阶段 6（业务域纵深）逐域推进中，唯独**系统管理域**空白，是 ③ 类只读域后端化的最后一项、也是唯一带**写侧 + 鉴权邻域**的一项：

- **数据模型残缺**：只有 `sys_user`（单角色字符串列）与 `sys_menu`（`allowed_roles` 逗号串）两张表；无 `sys_role`、无用户/角色关联、无权限码注册表、无字典表（全库检索 `sys_role|sys_dict|SysRole|Dict` 零命中）。
- **前端是空占位**：`frontend-scaffold/src/views/system/users.vue` 只有一行「模块开发中」文案 + 两个 `v-permission` 演示按钮，**未 import 任何 service**；项目列表/角色/字典无任何字段依据。
- **权限链路口径不一致**：前端权限码写死在 `stores/auth.ts` 的 `ROLE_PERMS`（仅 `admin` 一个角色，硬编码全部权限），后端 `@RequireAuth` 只能按单角色名判定（`security/RequireAuth.java` 仅有 `value` / `role` 两个属性），两端各自维护一套权限口径。
- **存在一处潜在安全缺陷**：`AuthService.refresh()` 换发新 access 令牌时**硬编码角色 `"ADMIN"`**（`service/AuthService.java` 行 92），不还原库中真实角色。当前只有单一管理员角色故为潜伏态，一旦引入多角色即成为提权漏洞。

不做这一项，阶段 6 无法形成完整纵向闭环；前端 `rbac-permission` capability 声明的「动态路由与菜单权限 / 按钮级权限 / 角色-终端-防区映射」也无后端事实支撑。

## What Changes

- **新增 RBAC 数据模型**（V32+ 增量迁移，三方言同步）：`sys_role`（角色）、`sys_role_menu`（角色-菜单/权限授权）、`sys_dict_type` + `sys_dict_item`（数据字典）；`sys_menu` 扩列 `menu_type` / `perm_code` / `visible`；`sys_user` 扩列 `pwd_updated_at` / `must_change_pwd`。
- **权限判定链路升级**：新增权限码判定（`@RequireAuth(perm="system:user:create")`）+ 角色→权限码解析组件（Caffeine 读穿 + 写时失效，复用 `IdNameCacheService` 既有范式）；`GET /auth/me` 增补 `roles` / `perms` 字段作为前端权限唯一来源，退役前端硬编码 `ROLE_PERMS`。
- **新增系统管理域端点**：用户/角色/菜单权限/字典 四组 CRUD（`/api/v1/system/**`），全部 `ADMIN`/权限码门禁，含自锁与提权防护、最后管理员保护。
- **个人中心端点**：改本人资料、改本人密码、管理员重置他人密码（随机临时密码 + 强制首登改密）。
- **修复 `AuthService.refresh` 角色硬编码缺陷**（安全修复，随本 Change 落地）。
- **服务端审计**：系统管理写操作由服务端主动落 `fac_audit_log`（补齐当前「仅靠前端上报」的缺口）。

## Capabilities

### Added Capabilities

- `system-management`：系统管理域（用户 / 角色 / 菜单权限 / 字典）的查询与变更端点、自锁与提权防护、服务端审计。
- `password-lifecycle`：口令生命周期（本人改密、管理员重置、强制首登改密、复杂度校验）。

### Modified Capabilities

- `auth-rbac`：RBAC 模型由「`sys_user.role` 单字符串 + `sys_menu.allowed_roles` 逗号串」升级为「`sys_role` 角色表 + `sys_role_menu` 授权表 + 权限码判定」；`/auth/me` 增补 `roles`/`perms`；`@RequireAuth` 增补 `perm` 属性。
- `rbac-permission`（前端）：权限码来源由前端硬编码 `ROLE_PERMS` 改为后端 `/auth/me` 下发；角色集合由单一 `admin` 扩展为可配置角色表。
- `backend-security-baseline`：**不改**——本 Change 不动过滤器顺序、CORS、密钥策略；仅在既有 `JwtFilter` 语义内工作。

## Impact

- **端**：后端 `controller` / `service` / `mapper` / `entity` / `dto` 新增；`security/` 仅**加**属性与方法（`RequireAuth.perm`、权限解析组件），**不改**令牌结构、过滤器顺序、白名单。前端 `views/system/*`、`stores/auth.ts`、`services/system.ts`、`router/menu.ts` 调整。
- **契约**：新增 `docs/api/system.openapi.json`；修改 `docs/api/auth.openapi.json`（`/auth/me` 增 `roles`/`perms`、增 `/auth/password`、`/auth/profile`）。走四同步（openspec → 前端契约 → 后端实现 → `npm run gen:api-types`）。
- **数据库**：新增 V32+ 增量迁移（h2/postgresql/dameng 三方言一致）；**存量影响**：`sys_user.role` 语义由「自由字符串」收紧为「引用 `sys_role.role_code`」；`sys_menu.allowed_roles` 数据迁移至 `sys_role_menu` 后降级为只读兼容列（见 `design.md §5.4`）。
- **回归**：后端单测基线（当前 361）须保持全绿并新增覆盖；前端 `vitest` / `vue-tsc` / 契约校验须全绿；`check-api-contract.mjs --strict` 路由与 schema 零漂移。
- **风险等级**：**高**。涉及鉴权判定与数据库结构，误改可导致越权或全员锁死（无管理员可用）。回退方案见 `design.md §14`。

## 非目标（本 Change 明确不做）

- ❌ ABAC 防区数据过滤（`sys_role.data_scope` 仅**预留列**，本期不实现行级过滤）——留给独立 Change。
- ❌ 账号锁定 / 登录失败次数风控（避免与登录链路耦合，登记为后续）。
- ❌ 多角色（一个用户多角色）——本期为**单角色**模型，模型设计为可加性扩展（见 `design.md` ADR-1）。
- ❌ IDP / SSO / 统一认证对接、多租户。
- ❌ 操作日志查询页（本期只做**写入**审计，不做审计查询 UI）。

## 人工确认关卡（L4 须过，逐条勾选后方可开工）

- [x] **需求确认**：系统管理域范围（用户/角色/菜单权限/字典 四组 CRUD）与「非目标」清单与预期一致。
- [x] **架构决策**：ADR-1 单角色 RBAC、ADR-2 统一菜单树承载权限码、ADR-3 权限码经服务端缓存解析（不塞令牌）——三项决策被确认。
- [x] **权限确认**：`design.md §7` 权限边界矩阵、自锁/提权/最后管理员三类防护规则被确认。
- [x] **数据库结构确认**：`design.md §5` DDL 草案与三方言迁移策略、`sys_menu.allowed_roles` 降级方式被确认。
- [x] **安全修复确认**：`AuthService.refresh` 角色硬编码缺陷随本 Change 修复。
- [x] **未决问题**：`design.md §14` 的 7 项待决问题已裁决（全部按推荐结论）。
- [x] **前端镜像**：确认前端 Change 命名与归属——本 Change 同批顺带落地前端（`frontend-scaffold` 侧改动并入同一次交付，不单建镜像 Change 目录，理由：前端改动为同一 capability 的从属面，避免两套并行 task 源）。
