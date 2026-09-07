# 设计文档：后端安全配置与工程正确性加固

> 与本变更 `proposal.md` / `tasks.md` / `spec-delta.md` 四者闭环。

## 目标与约束

- **目标**：让 `AGENTS.md` 宣称的红线在代码里真实成立——生产签名必开、CORS 收敛、过滤器顺序确定、方言正确、出参有类型、改动有测试保护。
- **硬约束**：不得改 `Result` 包络与 `ResultCode` 分段；不得动硬控名单与零下行控制语义；不得改 JWT 签发与无状态语义；不得引入新生产依赖（测试依赖除外）。

## 架构与方案

### 1. 生产配置基线

新增 `application-prod.yml`，覆盖四项：

| 配置项                       | 值                    | 理由                                                     |
| ---------------------------- | --------------------- | -------------------------------------------------------- |
| `signature.enabled`          | `true`                | §3.4 生产强制防重放；不再依赖 `SIGNATURE_ENABLED` 默认值   |
| `spring.h2.console.enabled`  | `false`               | 生产不得暴露 H2 Console                                   |
| `spring.jpa/mapper` 日志     | 关闭 SQL 打印         | 避免生产日志膨胀与敏感数据落盘                             |
| `jwt.secret`                 | `${JWT_SECRET}`（无默认） | 缺环境变量即启动失败，避免带占位密钥上线               |

`jwt.secret` 在 prod 去掉默认值是关键：当前 `application.yml` 的 `${JWT_SECRET:...change-in-production...}` 会让"忘记配环境变量"静默通过。

### 2. CORS 收敛

`WebMvcConfig` 引入 `app.cors.allowed-origins`（`List<String>`），dev 用 `@Value` 默认给 `http://localhost:5173` 等四个前端端口，prod 由环境变量注入具体域名。判断逻辑：仅当列表含 `*` 时才用 `allowedOriginPatterns`，否则用 `allowedOrigins`。`/ws/**` 同规则。

保留 `allowCredentials(true)`，但只有在来源白名单化的前提下才安全。

### 3. 过滤器顺序显式化

现状：`HmacFilter`、`JwtFilter` 均为 `@Component extends OncePerRequestFilter`，由 Spring Boot 自动注册为全局 Filter，**相对顺序由 bean 名哈希决定，不受控**。

方案：移除两处的 `@Component` 自动注册语义，改在 `SecurityBeans` 中用 `FilterRegistrationBean` 显式声明并 `setOrder`：

- `HmacFilter` → order `Ordered.HIGHEST_PRECEDENCE`（先校验签名）
- `JwtFilter` → order `Ordered.HIGHEST_PRECEDENCE + 10`（再解析身份）

拦截器链（HardControl → RequireAuth）保持 `WebMvcConfig` 现有注册不变，天然在 Filter 之后执行，因此最终顺序为：

```
HmacFilter(1) → JwtFilter(2) → HardControlInterceptor(3) → RequireAuthInterceptor(4)
```

与 `AGENTS.md` §6.1 宣称一致。

### 4. 分页方言

`new PaginationInnerInterceptor(DbType.POSTGRE_SQL)` 写死方言，在 dev 的 H2 下会生成 PG 分页 SQL。方案：改为按 `spring.datasource.url` 前缀推断，或直接使用无参构造 `new PaginationInnerInterceptor()` 让 MyBatis-Plus 依据 JDBC 元数据自动判定。选后者——最少代码、无需维护映射表。

### 5. 出参 DTO 化与去 mock

新增 `dto/DevicePageResult.java`（字段 `list` / `total` / `page` / `size`，与前端 `_shared.json` 的 `PageResult` 完全同构）。`DeviceController.page()` 返回 `Result<DevicePageResult>`，删除 `devFallbackList` 分支。

**去 mock 的影响**：空库时 `GET /api/v1/devices` 由"12 条假数据"变为"空列表 + total=0"。这是正确行为（§1.3：桩数据不得算完成），但会打破 `smoke-test.ps1` 第 4 步，需同步改断言为"请求成功且 `data.list` 存在"。

### 6. 测试基线

按 §2.2 起步基线，只做零依赖单测（不起 Spring 上下文、不连库、不用 Testcontainers）：

| 层         | 测试类                       | 手法                                              |
| ---------- | ---------------------------- | ------------------------------------------------- |
| Controller | `DeviceControllerTest`       | `MockMvcBuilders.standaloneSetup` + mock Service   |
| Controller | `AuthControllerTest`         | 同上，覆盖登录成功 / 口令错误两条路径              |
| Service    | `DeviceServiceTest`          | 纯 Mockito，mock Mapper                            |
| 安全层     | `JwtUtilTest`                | 签发 / 解析 / 过期 / 篡改                          |
| 安全层     `HardControlInterceptorTest` | 硬控路径 POST 命中即拒、GET 放行 | 直接单测 `preHandle` |
| 契约       | `ResultEnvelopeTest`         | `Result.ok` / `Result.fail` 包络字段与 traceId     |

TDD：先写测试跑红，再改实现。

## 决策记录（ADR）

- **决策 1**：过滤器顺序用 `FilterRegistrationBean` 而非 `@Order` 注解 —— 理由：`@Order` 对 Spring Boot 自动注册的 Filter 生效但不直观，`FilterRegistrationBean` 把顺序写在装配处，与 §6.1 文档一一对应 —— 反对项：多写约 20 行配置代码。
- **决策 2**：分页方言用无参构造而非 profile 分支 —— 理由：MyBatis-Plus 能依 JDBC URL 自动判定，避免维护第二套"哪个 profile 用哪个库"的真相源 —— 反对项：自动判定在极少数驱动上可能失败（本项目 H2 / PG 均为主流驱动，风险可接受）。
- **决策 3**：直接移除 mock 回落而非加开关 —— 理由：留开关等于把桩数据合法化，与 §1.3 冲突 —— 反对项：空库时前端页面看不到演示数据（可接受，本来就该由 `data.sql` 提供种子数据）。
- **决策 4**：`jwt.secret` 在 prod 去掉默认值 —— 理由：让"忘记配置"立即启动失败，优于静默使用弱密钥 —— 反对项：部署时需确保环境变量已注入（README 已记录）。

## 风险与缓解

| 风险                                    | 可能影响                        | 缓解措施                                                           |
| --------------------------------------- | ------------------------------- | ------------------------------------------------------------------ |
| 移除 mock 后 dev 空库演示无数据         | 前端联调看不到设备列表          | 在 `data.sql` 补种子设备数据（本变更内一并处理）                    |
| 过滤器改为显式注册后漏注册              | 生产签名或鉴权失效              | `HardControlInterceptorTest` + 启动冒烟覆盖；QA 记录实际执行结果     |
| CORS 白名单配置缺失导致前端 403         | 跨域请求被拒                    | dev profile 给默认四个端口；prod 由环境变量注入，README 注明         |
| `jwt.secret` 无默认值导致 prod 起不来   | 部署失败                        | 这是预期行为；README 与 `application-prod.yml` 注释均写明            |
| 分页方言自动判定失败                    | 分页 SQL 报错                   | 冒烟第 4 步直接覆盖分页查询，回归时能立刻发现                        |

## 依赖

- 上游：`AGENTS.md` §3 契约、§6 约束、§8 L4 门禁；前端 `_shared.json` 的 `PageResult` 定义。
- 下游：前端设备列表页（消费 `GET /api/v1/devices`）、`scripts/smoke-test.ps1`。
- 待确认项：`#TODO-确认` 401/403 是否应同时返回真实 HTTP 状态码。当前实现一律 HTTP 200 + 业务 code，前端 `unwrapBody` 可工作，但与前端契约里声明的 `Unauthorized` / `Forbidden` 响应不一致。本变更**不改**该行为，留待契约补齐时与前端确认。
