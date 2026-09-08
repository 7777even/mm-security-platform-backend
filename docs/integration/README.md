# 前后端联调运行手册

本手册面向需要在本地把 `backend-scaffold`（后端）与 `frontend-scaffold`（前端）跑通联调的开发者。
**目标定位**：dev 级 —— 后端以 `dev` profile（H2 内存库 + Flyway 自动建表/种子）离线即起，为前端脚手架提供真实接口；生产库（达梦 DM8 / PostgreSQL）迁移 hardening 不在 dev 目标内。

---

## 1. 启动拓扑

| 角色   | 默认地址                          | 说明                                              |
| ------ | --------------------------------- | ------------------------------------------------- |
| 后端   | `http://localhost:8787/api/v1`    | dev profile 端口 `8787`，对齐前端 `VITE_API_BASE` |
| 后端 WS | `ws://localhost:8787/ws/alarm`    | 告警实时推送，对齐前端 `VITE_ALARM_WS_URL`        |
| 前端   | `http://localhost:5173`（vite 动态分配，可能 5174/5175…） | `npm run dev`                 |

后端启动（dev，无需本地数据库）：

```bash
cd backend-scaffold
mvn -s ci-settings.xml spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8787"
# 注意：本机若被注入 SERVER__PORT 环境变量抢端口，必须用 jvmArguments 显式指定 -Dserver.port，
# 仅写 -Dserver.port 会被传进 Maven 自身 JVM 而非 fork 出的应用 JVM，回落到 SERVER__PORT 仍抢错端口。
```

前端启动（已关 dev mock，直连真后端）：

```bash
cd frontend-scaffold
npm install            # 含 Cesium，首次较慢；无 lockfile
npm run dev            # 浏览器开输出的本地端口
```

---

## 2. 鉴权与 dev 凭证

- 后端启动后由 `AuthService.ensureAdmin()` 写入默认账号 **`admin` / `admin@2026`**。
- 前端 `src/main.ts` 在启动阶段调用 `login()`（`POST /api/v1/auth/login`）拿 `accessToken` 存入内存态，
  请求拦截器（`src/services/http.ts`）自动注入 `Authorization: Bearer <token>`。
- **401 处理**：响应拦截器捕获 401 后清内存令牌并通过 `onUnauthenticated` 钩子跳登录，
  不再依赖本地写死的假 token（早期 `mock-admin-*` 假令牌会被 `JwtFilter` 拒，关 mock 后必失败）。
- dev 凭证可由前端 `.env.development` 的 `VITE_DEV_USER` / `VITE_DEV_PASSWORD` 覆盖（已写入 `env.d.ts` 类型）。

---

## 3. 环境变量对照

| 变量                      | 位置                       | 值 / 说明                                                  |
| ------------------------- | -------------------------- | --------------------------------------------------------- |
| `VITE_API_BASE`           | frontend `.env.development` | `http://localhost:8787/api/v1`（默认即指 8787，零前端改动） |
| `VITE_ALARM_WS_URL`       | frontend `.env.development` | `ws://localhost:8787/ws/alarm`（**必须显式绝对地址**，否则相对路径解析到 vite 端口连错主机） |
| `VITE_USE_DEV_MOCK`       | frontend `.env.development` | `false`（关闭全局 mock 适配器，请求直连真后端）            |
| `VITE_DEV_USER` / `VITE_DEV_PASSWORD` | frontend `.env.development` | dev 登录账号（缺省 `admin` / `admin@2026`）              |
| `server.port`             | backend `application-dev.yml` | `8787`                                                    |
| `JWT_SECRET` / `DB_PASSWORD` / `SIGNATURE_SECRET` | 后端环境变量 | 生产强制注入；dev 用 `application-dev.yml` 占位弱密钥（禁止上生产） |

> `.env.development` 里另有遗留未引用的 `VITE_WS_BASE=ws://localhost:8787/ws`，`realtime.ts` 并不读取它，勿被误导。

---

## 4. 接口契约

- **机器可读契约唯一真源在前端库**：`frontend-scaffold/docs/api/*.openapi.json`（API Contract First，
  前端由它 `npm run gen:api-types` 生成 `src/types/generated/**`）。后端不复制第二份主契约。
- **统一 B3 包络**：`{ code, message, data, traceId }`，`code=0` 成功；非 0 由 `GlobalExceptionHandler` 统一转包络。
  前端 `ApiResponse<T>` 字段完全一致。
- **错误码**：401 未认证 / 403 无权限已映射真实 HTTP 状态码（不再永远 200）；鉴权失败 `JwtFilter` 直接写 401 + B3 包络，
  不抛异常冒泡（否则变 500 且无 CORS 头）。
- 后端改动接口后须 `node scripts/check-api-contract.mjs` 校验端点不漂移，并通知前端重生成类型（四同步见 `AGENTS.md` §11）。

---

## 5. WebSocket 实时推送（`/ws/alarm`）

契约真源：`frontend-scaffold/docs/api/realtime.openapi.json`。

- **推送包络（服务端 → 客户端）**：`{ "topic": "alarm.push", "payload": <AlarmItem> }`。
  ⚠️ 历史上后端曾发 `{type, data}`，前端只认 `topic`，导致推送被静默丢弃；现已对齐契约。
- **客户端心跳（契约约定）**：前端每 15s 发 `{ "type": "ping" }`；后端已显式处理，保持连接。
- 推送源：`AlarmSimulator` 每 12s 推最新一条告警（增量语义，仅推未推过的最新告警；如需验证端到端，
  可通过 `POST /api/v1/alarms` 造一条新告警触发即时推送）。
- 鉴权：`/ws` 在 `JwtFilter.WHITELIST` 内免 JWT，连接即订阅只读监视流。

---

## 6. CORS 规则

- **dev profile**：`app.cors.allowed-origins: "*"`（vite 端口动态，用 `allowedOriginPatterns("*")` + `allowCredentials` 兼容任意 localhost）。
- **prod / dm profile**：`app.cors.allowed-origins: ${CORS_ALLOWED_ORIGINS}`（环境变量强制注入真实域名白名单，
  **不含 `*`**）；`CorsConfig` 在**非 dev profile 且含 `*` 时启动即抛异常 fail-fast**，杜绝生产误配通配。
- CORS 由 Servlet 级 `CorsFilter`（HIGHEST_PRECEDENCE，先于 `JwtFilter`/`HmacFilter`）统一加头，
  保证被鉴权短路的 401 响应也带 `Access-Control-Allow-Origin`，浏览器不报 CORS 阻断。

---

## 7. 冒烟验证

后端契约/链路冒烟（PowerShell）：

```bash
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

最小人工验收路径：登录拿 token → `/api/v1/auth/menus` 200 → `/api/v1/devices` 200 →
`/api/v1/dashboard/overview` 200 → `ws://localhost:8787/ws/alarm` 收到 `{topic:'alarm.push',...}`。

后端测试基线（regression 门禁）：

```bash
mvn -s ci-settings.xml test     # 含 security/config/websocket/integration 包，横切层改动须全绿
```

其中 `integration/IntegrationContractTest`（standalone MockMvc，不起 Spring 上下文）串起
`CorsFilter → HmacFilter → JwtFilter` 与探针控制器，锁死「401 响应带 CORS 头」「白名单放行」「合法 Bearer 通过」三类契约。
