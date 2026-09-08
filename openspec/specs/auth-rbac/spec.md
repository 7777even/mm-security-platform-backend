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

系统须基于 `sys_user.role` 与 `sys_menu.allowed_roles`（逗号分隔）实现 RBAC；端点级角色门禁用 `@RequireAuth(role=...)`，资源级归属校验用 `AuthorizationService.assertSelfOrAdmin`，不可将角色硬编码进业务逻辑。

#### Scenario: 端点角色门禁

- **WHEN** 非 `ADMIN` 角色访问标注 `@RequireAuth(role="ADMIN")` 的端点
- **THEN** 返回 403，不执行业务逻辑

#### Scenario: 资源归属越权

- **WHEN** 普通用户尝试操作非本人创建、且非管理员可越权的资源
- **THEN** `assertSelfOrAdmin` 抛出 403，不执行业务逻辑

#### Scenario: 菜单按角色过滤

- **WHEN** 某角色请求 `GET /api/v1/auth/menus`
- **THEN** 返回仅含 `sys_menu.allowed_roles` 命中当前角色的菜单

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
