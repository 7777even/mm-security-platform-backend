# auth-rbac Specification

## Purpose

认证与基于角色的访问控制（RBAC）基线：无状态 JWT 双令牌、刷新令牌 Cookie 化、角色门禁与越权守门、鉴权失败语义、密钥 fail-fast。设计依据见 `docs/architecture/auth-design.md`；与 `backend-security-baseline`（CORS / 过滤器顺序 / 生产配置）互补，本 spec 只覆盖认证与授权语义。

## Requirements

### Requirement: 无状态 JWT 双令牌

系统须以无状态 JWT 签发 access 与 refresh 两类令牌，服务端不存储任何令牌状态（无 refresh 表、无 token 黑名单）；access 令牌须携带 `type=access` claim，refresh 令牌仅可用于换发、不可作为 access 使用。

#### Scenario: access 令牌校验类型

- **WHEN** 请求携带 `type` claim 不为 `access` 的 JWT（如 refresh 令牌）
- **THEN** `JwtFilter` 拒绝该请求（401），不将其当作有效 access 令牌

#### Scenario: 无服务端令牌状态

- **WHEN** 检视令牌签发与校验实现
- **THEN** 不存在 refresh 表 / token 黑名单等任何形式的服务端令牌状态存储

### Requirement: 刷新令牌 Cookie 化

refresh 令牌不得进入响应 body，仅经 `HttpOnly` + `SameSite=Lax` Cookie 下发（`name=rt`）；`/auth/refresh` 从 Cookie 读取、`/auth/logout` 下发同名 `Max-Age=0` Cookie 清除。规避 XSS 窃取刷新令牌。

#### Scenario: 登录下发刷新 Cookie

- **WHEN** `POST /api/v1/auth/login` 成功
- **THEN** 响应经 `Set-Cookie` 下发 `HttpOnly` + `SameSite=Lax` 的 `rt` Cookie，且响应 body 不含 refresh 令牌字段

#### Scenario: 登出清除刷新 Cookie

- **WHEN** `POST /api/v1/auth/logout`
- **THEN** 响应下发同名 `rt` Cookie 的 `Max-Age=0` 清除指令

### Requirement: 角色门禁与越权守门

系统须基于 `sys_role`（角色）与 `sys_role_menu`（角色-菜单/权限授权）实现 RBAC，权限码即 `sys_menu.perm_code`（`menu_type` = DIR / MENU / BUTTON，BUTTON 不参与导航、只贡献权限码）；`sys_user.role`（**单角色**）引用 `sys_role.role_code`。`sys_menu.allowed_roles` 为遗留列，**不得**再参与任何判定。端点级门禁支持 `@RequireAuth(role=...)`（单角色名）与 `@RequireAuth(perm=...)`（权限码，经 `RoleAuthorityService` 解析，二者同时标注为 AND）；资源级归属校验沿用 `AuthorizationService.assertSelfOrAdmin`；不可将角色硬编码进业务逻辑。角色授权变更须**即时生效**（授权缓存写时失效，不等待令牌过期）。

#### Scenario: 端点角色门禁

- **WHEN** 非 `ADMIN` 角色访问标注 `@RequireAuth(role="ADMIN")` 的端点
- **THEN** 返回 403，不执行业务逻辑

#### Scenario: 端点权限码门禁

- **WHEN** 当前角色不持有 `@RequireAuth(perm="system:user:create")` 所要求的权限码
- **THEN** 返回 403，不执行业务逻辑

#### Scenario: 角色授权变更即时生效

- **WHEN** 管理员通过 `PUT /api/v1/system/roles/{id}/menus` 修改某角色授权
- **THEN** 该角色用户下一次请求的权限解析即为新结果（`reloadRolePerms()` 主动失效，不依赖 5min TTL）

#### Scenario: 资源归属越权

- **WHEN** 普通用户尝试操作非本人创建、且非管理员可越权的资源
- **THEN** `assertSelfOrAdmin` 抛出 403，不执行业务逻辑

#### Scenario: 菜单按角色过滤

- **WHEN** 某角色请求 `GET /api/v1/auth/menus`
- **THEN** 返回仅含该角色经 `sys_role_menu` 授权、且 `menu_type ∈ {DIR,MENU}`、`visible=1`、顶层 `fm-*` 的菜单

### Requirement: 登录身份下发

`GET /api/v1/auth/me` 须下发当前用户的 `username`、`realName`、`role`、`roles`、`perms`、`mustChangePwd`；`perms` 为该用户角色经 `sys_role_menu` 聚合去重后的权限码全集，是前端权限判定的**唯一权威来源**（前端不得再维护硬编码角色权限表）。续期换发的 access 令牌须携带**库中真实角色**，不得硬编码；账号不存在或已停用时拒绝续期。

#### Scenario: 权限码下发

- **WHEN** 携带有效令牌调用 `GET /api/v1/auth/me`
- **THEN** 返回的 `perms` 与后端门禁判定所用权限码同源一致

#### Scenario: 续期不硬编码角色

- **WHEN** 角色为 `X` 的用户经 `POST /api/v1/auth/refresh` 换发新 access 令牌
- **THEN** 新令牌角色仍为库中真实角色 `X`，不得被改写为 `ADMIN`

#### Scenario: 已停用账号拒绝续期

- **WHEN** 账号已被停用（`status=0`）但持有未过期 refresh Cookie
- **THEN** `POST /api/v1/auth/refresh` 返回 401，不签发新令牌

### Requirement: 鉴权失败语义

缺/过期/非法令牌或签名 → **401**；角色不符或数据级越权 → **403**；均经 `GlobalExceptionHandler` 包成 `Result<T>`，不冒泡 500，且响应带 CORS 头（由最前的 `CorsFilter` 保证）。

#### Scenario: 缺令牌访问受保护端点

- **WHEN** 未携带有效 access 令牌调用 `GET /api/v1/auth/me`
- **THEN** 返回 HTTP 401 且 body 为 `Result.fail`，而非 200 + 业务码

### Requirement: 免鉴权白名单口径

`/api/v1/auth/login`、`/api/v1/auth/refresh`、`/api/v1/auth/logout` 免鉴权；`/auth/me` 与 `/auth/menus` **必须携带有效 access 令牌**，不得回到白名单（否则 `UserContext` 不注入，越权暴露真实身份/全量菜单）。

#### Scenario: 白名单外受保护端点

- **WHEN** 未携带令牌调用 `GET /api/v1/auth/menus`
- **THEN** 返回 401（`UserContext` 未注入，无法取真实身份/全量菜单）

### Requirement: 密钥 fail-fast

JWT / 签名密钥仅经环境变量 `JWT_SECRET` / `SIGNATURE_SECRET` 注入（base `application.yml` 不写默认值）；缺失或弱密钥（`jwt.secret` < 32 字节 / `signature.secret` < 16 字节 / 已知占位）时启动失败，绝不回退占位密钥。

#### Scenario: 缺失或弱 JWT 密钥

- **WHEN** 启动且未注入 `JWT_SECRET` 或密钥长度/占位不合规
- **THEN** `SecurityBeans.validateSecrets()` 抛 `IllegalStateException`，应用拒绝启动

### Requirement: 行级 ABAC（data_scope）与接口鉴权正交

行级数据访问控制须由 `sys_role.data_scope`（取值 `ALL` / `DEPT` / `SELF`，`V33` 已落库）与 `sys_user.zone_codes`（逗号串，如 `炼油区,罐区`）共同决定；`data_scope=ALL`（典型即 `ADMIN`）表示看全部行，解析器返回 `null`、调用方不加 WHERE。`data_scope` 与 `@RequireAuth(role/perm)` 接口级鉴权**正交叠加**：前者控制「能看到哪些数据行」，后者控制「能否进入端点」。解析由服务端 `DataScopeResolver`（复用 `RoleAuthorityService` 缓存 + 用户维度 `userZoneCache` Caffeine，TTL 5min，用户 `zone_codes` 变更经 `invalidateUser` 写时失效）完成，**令牌仍只携 `role`**（权限码与防区均不写入令牌，遵循 ADR-3）。因各业务实体防区列名不统一，采用 `DataScopeHelper.apply(qw, zoneColumn, zones)` 由各 Service 显式注入（ADR-4），三态语义：`zones==null`→不加条件（ALL）；`zones` 为空集→`1=0`（最小权限，看不到任何行）；`zones` 非空→`IN (zones)`。

#### Scenario: ALL 范围不加数据条件

- **WHEN** `ADMIN`（或其 `data_scope=ALL` 的角色）查询接入 ABAC 的列表端点
- **THEN** 查询不带任何防区 WHERE 条件，返回全部行

#### Scenario: SELF 范围按用户防区过滤

- **WHEN** 某 `data_scope=SELF` 用户拥有 `zone_codes=炼油区,罐区` 查询接入 ABAC 的列表
- **THEN** 查询仅返回 `zone` 列 ∈ {炼油区,罐区} 的行；其 `zone_codes` 变更经 `invalidateUser` 后下次请求即生效

#### Scenario: 空防区集合最小权限

- **WHEN** 某 `data_scope=SELF` 用户的 `zone_codes` 为空或缺失
- **THEN** 查询被注入 `1=0`，不返回任何行

#### Scenario: 匿名公开端点不过滤

- **WHEN** 公开端点（如救援队伍域只读接口）在未登录状态下被访问
- **THEN** `resolveZones()` 返回 `null`，不加防区条件，保持原有公开行为（仅已登录用户才受 ABAC 限制）

#### Scenario: 防区与业务列取值对齐

- **WHEN** 接入 ABAC 的业务域（首期=救援队伍域 `FacBrigadeTeam.area`）配置防区
- **THEN** 其取值须与 `sys_zone.zone_name` 及对应字典标签（如 `BRIGADE_AREA`）精确一致，否则 `IN` 条件命中不到任何行
