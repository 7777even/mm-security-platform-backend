# 过滤器 / 拦截器链（Request Pipeline）

> 请求处理顺序的权威说明。任何顺序变更属 **L4**（高风险），须 proposal + 评审。本文与 `AGENTS.md §6`、契约白名单口径互为镜像。

## 1. 顺序（不可乱）

| # | 组件 | 类型 | Order | 职责 |
| - | ---- | ---- | ----- | ---- |
| 0 | `CorsFilter` | Filter (`CorsConfig`) | `HIGHEST_PRECEDENCE` | **最先**写入 CORS 头，保证被后续短路的 401/403 响应也带 `Access-Control-Allow-Origin` |
| 1 | `HmacFilter` | Filter (`SecurityBeans`) | `HIGHEST_PRECEDENCE+1` | 生产开启：校验 `X-Timestamp` / `X-Nonce` / `X-Signature`（HMAC-SHA256，容忍 300s）；dev 关闭 |
| 2 | `JwtFilter` | Filter (`SecurityBeans`) | `HIGHEST_PRECEDENCE+10` | 解析 `Bearer` Token → 写 `UserContext`；**白名单路径跳过** |
| 3 | `HardControlInterceptor` | Interceptor | — | POST/PUT/DELETE 命中 `HARD_CONTROL_PATHS` → 503 `HARD_CONTROL_BLOCKED`（零下行控制红线） |
| 4 | `RequireAuthInterceptor` | Interceptor | — | `@RequireAuth(role=ADMIN)` 鉴权；数据级 `assertSelfOrAdmin` |
| 5 | Controller → Service → Mapper | — | — | 业务处理 |
| 6 | `GlobalExceptionHandler` | `@ControllerAdvice` | — | 统一 `Result<T>` 包络；鉴权失败 → 401/403（非 500） |

顺序由 `config/SecurityBeans` 用 `FilterRegistrationBean.setOrder` **显式声明**——不再依赖 Spring 对 `@Component` Filter 的自动注册（顺序由 bean 名哈希决定，不可控）。

## 2. 为什么 CorsFilter 必须最前

CORS 预检（`OPTIONS`）与跨域鉴权失败响应都需带 CORS 头，否则浏览器拦截、前端无法读取 401/403 触发跳登录。若 Hmac/Jwt 先于 Cors 短路，响应缺 `Access-Control-Allow-Origin` → 前端联调时「接口 401 但页面无感知」。集成测试 `IntegrationContractTest` 锁死此不变量（401 响应必带 CORS 头）。

## 3. JwtFilter 白名单（`WHITELIST`）

免鉴权路径（不写 `UserContext`、直接放行）：

```
/api/v1/auth/login
/api/v1/auth/refresh
/api/v1/auth/logout
/actuator          （含 /actuator/health、/actuator/prometheus，限内网）
/h2-console        （dev 仅）
/ws                （WebSocket 握手，令牌走子协议/查询参数）
/error
```

**`/auth/me` 与 `/auth/menus` 已移出白名单**——契约 `auth.openapi.json` 声明 401/403，必须带有效 access 令牌，否则 `UserContext` 不注入、无法取真实身份 / RBAC 菜单。改这组白名单前先确认契约口径。

## 4. 鉴权失败语义

| 场景 | 行为 |
| ---- | ---- |
| 缺 Token / Token 过期 / 签名无效 | `JwtFilter` 返回 **401** + `Result.fail`（HTTP 状态码即 401，非 200+code） |
| `@RequireAuth(role=ADMIN)` 但角色不符 | `RequireAuthInterceptor` 返回 **403** |
| 数据级越权（`assertSelfOrAdmin` 失败） | `AuthorizationService` 抛异常 → 403 |
| 硬控路径写操作 | 503 `HARD_CONTROL_BLOCKED` |

所有失败均走 `GlobalExceptionHandler` 包成 `Result<T>`，**不冒泡为 500**；CORS 头由最前的 `CorsFilter` 保证存在。
