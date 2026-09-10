# 认证与授权设计（Authentication & Authorization）

> 认证域的权威设计说明。任何令牌结构、刷新策略、RBAC 模型、白名单口径的变更属 **L4**（高风险），须 proposal + 评审。本文与 `AGENTS.md §6.3 安全红线`、`filter-chain.md`、契约 `auth.openapi.json` / `uplink.openapi.json` 互为镜像。

## 1. 决策（Decisions）

- **无状态 JWT**：access 令牌由 `JwtUtil` 用 HS256 签发，**服务端不存储任何令牌状态**（无 refresh 表、无 token 黑名单）。`/auth/me`、`/auth/menus` 也依赖 `UserContext` 中的当前登录态，因此**必须携带有效 access 令牌**。
- **双令牌时长**：access 短效（`jwt.access-ttl`，默认 2h），refresh 长效（`jwt.refresh-ttl`，默认 7d）。`refresh` 类型令牌**仅用于换发**，不可当 access 用（`JwtFilter` 校验 `claim("type")=="access"`）。
- **刷新令牌 Cookie 化（S1 §5.3 合规红线）**：refresh 令牌**绝不进入响应 body**（`TokenResponse` 已移除 refresh 字段），仅经 `ResponseCookie` 下发 `HttpOnly` Cookie（`name=rt`、`SameSite=Lax`、`path=/`、`secure=app.cookie.secure`）。`POST /auth/refresh` 从 `@CookieValue` 读取，无请求体；登出下发同名 `Max-Age=0` Cookie 清除。规避 XSS 窃刷新令牌。
- **RBAC 模型（V32 起，2026-09-10 升级；取代原 `allowed_roles` 逗号串口径）**：`sys_user.role`（**单角色**）引用 `sys_role.role_code`；菜单与权限码的授权元组为 `sys_role_menu`；**权限码即 `sys_menu.perm_code`**（`menu_type` = DIR / MENU / BUTTON，BUTTON 不参与导航、只贡献权限码）。`sys_menu.allowed_roles` 为 V7 遗留列，**已降级为只读兼容列**（不再写入、不再参与判定），保留仅为可回退与历史对照。角色 → 授权解析由 `RoleAuthorityService` 提供（Caffeine `role_code→{menuIds,perms}`，TTL 5min + 写时失效 `reloadRolePerms()`）：**权限码不写入 access 令牌**，故角色授权变更**即时生效**且令牌不膨胀。端点级门禁用 `@RequireAuth(role=...)`（粗粒度单角色）或 `@RequireAuth(perm=...)`（权限码，经 `RoleAuthorityService` 判定；二者同时标注为 AND）；资源级归属校验用 `AuthorizationService.assertSelfOrAdmin`。系统管理域当前统一 `role="ADMIN"`（**渐进式**：权限码已全量登记，可逐端点切 `perm` 而不改路径）。
- **强制首登改密（服务端兜底）**：`must_change_pwd=1` 的账号在**变更类请求**（非 GET/HEAD/OPTIONS）上被 `PasswordLifecycleInterceptor` 拒绝（403）；豁免 `/api/v1/auth/**`（否则改密路径自身被堵死）与 `/api/v1/uplink/audit`（审计旁路）。状态经 `PasswordStateCache`（TTL 5min + 改密/重置时 evict）判定。前端引导跳改密页，但**不依赖前端**（否则直接用令牌调业务写接口即可绕过）。
- **启动期种子与锁死兜底**：原 `AuthService.ensureAdmin()` 拆出为 `RbacBootstrapService`（幂等）：角色按 `role_code` 逐个确保存在；默认管理员**仅在 `sys_user` 为空时**写入（置 `must_change_pwd`，由 `app.password.force-change-default-admin` 控制，dev 关闭 / 生产开启）；**仅当 ADMIN 一条授权都没有时**补齐全量启用节点作为锁死兜底（已有授权则不动，避免把有意收回的授权重新点亮）。
- **防重放**：生产开启 `HmacFilter`（`X-Timestamp` / `X-Nonce` / `X-Signature`，HMAC-SHA256，容忍 `signature.max-skew-seconds`=300s）；dev 关闭。
- **密钥 fail-fast**：`SecurityBeans.validateSecrets()` 启动期校验 `jwt.secret` ≥ 32 字节且非已知占位、`signature.secret` ≥ 16 字节且非已知占位，否则抛 `IllegalStateException` 拒绝启动。密钥**仅**经环境变量 `JWT_SECRET` / `SIGNATURE_SECRET` 注入，base `application.yml` 不写默认值。

## 2. 现状（Current State）

| 组件 | 类 / 文件 | 状态 |
| ---- | --------- | ---- |
| 令牌签发 / 校验 | `security/JwtUtil.java`（`issueAccess` / `issueRefresh` / `parse`） | ✅ 已落地 |
| 鉴权过滤器 | `security/JwtFilter.java`（白名单、`Bearer` 解析、401+B3 直写） | ✅ 已落地 |
| 刷新 Cookie | `controller/AuthController.java`（`addRefreshCookie` / `clearRefreshCookie`） | ✅ 已落地 |
| 角色门禁 | `security/RequireAuthInterceptor.java` + `@RequireAuth` | ✅ 已落地 |
| 越权校验 | `security/AuthorizationService.java`（`assertAdmin` / `assertSelfOrAdmin`） | ✅ 已落地 |
| 线程登录态 | `security/UserContext.java`（ThreadLocal，filter finally 清理） | ✅ 已落地 |
| 默认管理员 | `service/RbacBootstrapService.java`（角色幂等 upsert + `admin` / `admin@2026` + ADMIN 授权兜底） | ✅ 已落地（2026-09-10 自 `AuthService.ensureAdmin` 拆出） |
| 角色权限解析 | `security/RoleAuthorityService.java`（Caffeine `role_code→{menuIds,perms}`，写时失效） | ✅ 已落地（V32） |
| 权限码门禁 | `security/RequireAuth.perm` + `RequireAuthInterceptor` | ✅ 能力已落地（端点当前用 `role="ADMIN"`，可逐切 `perm`） |
| 强制改密兜底 | `security/PasswordLifecycleInterceptor.java` + `common/cache/PasswordStateCache.java` | ✅ 已落地 |
| 系统管理域审计 | `service/SystemAuditHelper.java`（写 `fac_audit_log`，`module=system`，尽力而为） | ✅ 已落地 |
| 过滤器顺序 | `config/SecurityBeans.java`（`FilterRegistrationBean.setOrder` 显式声明） | ✅ 已落地 |

白名单（`JwtFilter.WHITELIST`）：`/api/v1/auth/login`、`/api/v1/auth/refresh`、`/api/v1/auth/logout`、`/actuator`、`/h2-console`（dev）、`/ws`、`/error`。**`/auth/me` 与 `/auth/menus` 已移出白名单**。

## 3. 约束（Constraints）

- 令牌内存态：前端脚手架要求 access 令牌走内存（`token.ts`），**禁止 localStorage 明文**；后端 `JwtFilter` 注释已固化此约束。
- 鉴权失败语义：缺/过期/非法签名 → **401** + `Result.fail`（HTTP 状态码即 401，非 200+code）；角色不符 → **403**；数据级越权 → 403；均走 `GlobalExceptionHandler` 包成 `Result<T>`，**不冒泡 500**，CORS 头由最前的 `CorsFilter` 保证存在。
- `OPTIONS` 预检必须直接放行（见 `filter-chain.md §1`），否则浏览器收不到 CORS 头。
- 改白名单 / RBAC 口径前**先确认契约**（`auth.openapi.json` 声明 `me`/`menus` 401/403），再改代码（AGENTS §1.4 契约优先）。
- 密钥缺失或弱：启动即失败，绝不带默认密钥上线。

## 4. 反模式（Anti-patterns）

- ❌ 把 refresh 令牌塞进 `TokenResponse` body —— 必遭 XSS 窃取，违反 S1 §5.3。
- ❌ 在服务端维护 refresh 表 / token 黑名单 —— 破坏无状态前提，增加水平扩展与失效复杂度。
- ❌ 让 `/auth/me`、`/auth/menus` 回到白名单 —— 会让无令牌请求拿到真实身份 / 全量菜单，越权。
- ❌ 在 `JwtFilter` 之前短路且未带 CORS 头 —— 浏览器拦截 401，前端无感知。
- ❌ 把角色硬编码进业务逻辑而非读 `UserContext` / `sys_menu.allowed_roles` —— 失去 RBAC 可配置性。
- ❌ 用 `Map.of` 直接返回含敏感字段的实体（如 `passwordHash`）—— 见 `data-masking.md`。
