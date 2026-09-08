# docs/architecture/ — 后端架构与工程基线

> 长期共识文档：描述系统**现在**是什么样。改动代码后若下列任一项变化，必须回来同步本文件。

## 1. 技术栈与运行形态

| 项         | 值                                                                                              |
| ---------- | ----------------------------------------------------------------------------------------------- |
| 框架       | Spring Boot 3.2.5（Java 17）                                                                    |
| 持久层     | MyBatis-Plus 3.5.5（`map-underscore-to-camel-case`、逻辑删除 `deleted`）                        |
| 数据库     | **dev=H2 内存库 + Flyway（本地唯一可实跑）**；达梦 DM8（信创生产目标，暂缓未实证）；PostgreSQL（回退 profile，本机未装） |
| 认证       | JJWT 0.12.5，无状态；access 2h、refresh 7d（refresh 经 **HttpOnly Cookie** 下发，前端 JS 不可读） |
| 实时通道   | Spring WebSocket，`/ws/alarm` 告警推送（包络 `{topic, payload}` + 15s ping 心跳）                |
| 端口 / 前缀| dev 启动端口 **8787**（application.yml 默认 `8080` 被 `-Dserver.port=8787` 覆盖）/ 前缀 `/api/v1` |
| 构建       | 本机 Maven 唯一可用：`mvn -s ci-settings.xml`（Wrapper `mvnw` / 裸 `mvn` 在本机均不可用）          |

## 2. 分层与包职责

根包 `com.sinopec.mmsecurity`，九个包：

| 包            | 职责                                                 | 禁写                                                |
| ------------- | ---------------------------------------------------- | --------------------------------------------------- |
| `controller/` | 路由、参数绑定、`@Valid`、调用 Service               | 业务逻辑、自定义响应外壳、直接注入 Mapper           |
| `service/`    | 业务规则、事务边界、越权守门（`AuthorizationService`）| `HttpServletRequest`、吞异常、自行放行硬控          |
| `mapper/`     | 数据访问（MyBatis-Plus）                             | 业务判断；自定义 SQL 需同步 schema 与 IT            |
| `entity/`     | 与表 1:1 的持久化对象                               | 直接作为出参                                        |
| `dto/`        | 出入参对象                                           | 复用 Entity 做出参、字段无中文注释                  |
| `config/`     | WebMvc / MybatisPlus / WebSocket / 安全 Bean 装配    | 业务规则                                            |
| `security/`   | JWT / HMAC / 硬控 / 鉴权 / UserContext / 刷新 Cookie | 局部放宽（改动走 L4）                               |
| `common/`     | `Result` / `ResultCode` / `BusinessException` / `TraceContext` / `DeviceCode` | 局部改写包络与错误码            |
| `websocket/`  | 告警推送与模拟（`AlarmWebSocketHandler` / `AlarmSimulator`） | 借 WS 下发控制指令                          |

## 3. 请求链路（顺序不可乱）

```
HTTP 请求
  ↓ CorsFilter            (0) HIGHEST_PRECEDENCE：最先执行，保证被后续短路的鉴权响应也带 CORS 头
  ↓ HmacFilter            (1) HIGHEST_PRECEDENCE+1：生产开启，校验 X-Timestamp / X-Nonce / X-Signature，容忍 300s
  ↓ JwtFilter             (2) HIGHEST_PRECEDENCE+10：解析 Bearer Token，写入 UserContext；白名单路径跳过
  ↓ HardControlInterceptor(3) 硬控路径 POST/PUT/DELETE → 503 HARD_CONTROL_BLOCKED
  ↓ RequireAuthInterceptor(4) @RequireAuth 鉴权（角色 / 数据级 assertSelfOrAdmin）
  ↓ Controller → Service → Mapper
  ↓ GlobalExceptionHandler → common/Result<T> 统一包络
```

- 过滤器顺序由 `config/SecurityBeans` 用 `FilterRegistrationBean.setOrder` **显式声明**，不再依赖 Spring 对 `@Component` Filter 的自动注册（其顺序由 bean 名哈希决定，不可控）。**变更顺序属 L4**。
- `JwtFilter.WHITELIST`（免鉴权）：`/api/v1/auth/login`、`/api/v1/auth/refresh`、`/api/v1/auth/logout`、`/actuator`、`/h2-console`、`/ws`、`/error`。**`/auth/me` 与 `/auth/menus` 已移出白名单**——必须带有效 access 令牌，否则 `UserContext` 不注入（契约声明 401/403）。
- 鉴权失败 → 直接 401/403 + `Result.fail`（UTF-8），不冒泡 500。

## 4. 安全基线与错误码

- **零下行控制**：后端不提供下行控制写接口；`HardControlInterceptor.HARD_CONTROL_PATHS` 为唯一名单，新增名单外下行能力须人工评审。
- **密钥注入**：`JWT_SECRET` / `SIGNATURE_SECRET` 只从环境变量取（无默认，缺失即启动失败）；`SecurityBeans.validateSecrets()` 校验 JWT≥32 / 签名≥16 字节且非占位密钥。
- **刷新令牌 Cookie 化**：`login`/`refresh` 经 `ResponseCookie` 下发 `HttpOnly` Cookie（`name=rt`、`SameSite=Lax`、`Max-Age=7d`）；`refresh` 改 `@CookieValue` 读取、无请求体；`/auth/logout` 下发 `Max-Age=0` 同名 Cookie 清除。`app.cookie.secure` 开关（dev=false / prod,dm=true）。access 令牌仍由前端内存态持有。
- 日志脱敏：禁止打印令牌、口令、签名三头与完整敏感请求体；`traceId` 由 `TraceContext` 透传，用于前后端联调对齐。
- SQL：禁止字符串拼接，`${}` 一律禁用。

错误码分段（`common/ResultCode`）：

| 段    | 域     | 码                                                                        |
| ----- | ------ | ------------------------------------------------------------------------- |
| `1xx` | 通用   | `100` 参数非法、`401` 未认证、`403` 无权限、`404` 资源不存在               |
| `2xx` | 鉴权   | `201` TOKEN_EXPIRED、`202` TOKEN_INVALID、`203` SIGNATURE_INVALID、`204` SIGNATURE_EXPIRED |
| `3xx` | 设备   | `301` DEVICE_CODE_INVALID、`302` DEVICE_NOT_FOUND                          |
| `5xx` | 硬控   | `503` HARD_CONTROL_BLOCKED                                                 |

## 5. 数据层与迁移

- 迁移由 **Flyway** 接管（`spring.sql.init.enabled=false`，`spring.flyway.enabled=true`）。三套方言位于 `src/main/resources/db/migration/{h2,postgresql,dameng}/`，按 profile 由 `spring.flyway.locations` 指向：
  - `dev` → `h2`（H2 内存库，本地可实跑：V1 全量快照 ＋ V2 种子）。
  - `prod` → `postgresql`（回退 profile）。
  - `dm` → `dameng`（达梦 DM8 信创目标，**暂缓启用**：本机无 DM8 实例 / 驱动 / Docker，仅保留脚本与 `application-dm.yml` 作迁移资产）。
- `AuthService.ensureAdmin()` 在 Flyway 建好的 `sys_user` 上写默认账号 `admin` / `admin@2026`（不进种子脚本，避免重复）。
- **双轨策略**：V1 全量快照（空库直达最新）＋ 增量 `V<yyyyMMddHHmmss>__<snake>.sql`。**已进入共享环境的 V 文件禁止修改 / 重命名 / 删除**；后续 DDL：① 新增增量 V；② proposal 写明存量影响与回退。当前增量：V6 现场回传落库 / V7 `sys_menu.allowed_roles` + 重种 5 个 fm-* 顶部菜单 / V8 应急 4 张参考表。
- 逻辑删除统一 `deleted`（`0` 未删 / `1` 已删），新表须带审计字段（`created_at` 等）并在查询链路生效。
- 数据库规约详见 [`../database/README.md`](../database/README.md)。

## 6. 环境与运行

```bash
# dev（H2 兜底，签名关闭，端口 8787）
mvn -s ci-settings.xml spring-boot:run -Dserver.port=8787

# 单测（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）+ JaCoCo 行覆盖门禁 0.80
mvn -s ci-settings.xml test

# 冒烟（Python utf-8 脚本，避免中文 GBK 解码坑）
python scripts/smoke-test.py
```

`application.yml` 关键开关：`jwt.access-ttl`（7200s）、`jwt.refresh-ttl`（604800s）、`signature.enabled`（默认 `false`，生产须 `true`）、`signature.max-skew-seconds`（300s）、`app.cookie.secure`（dev=false / prod,dm=true）。

## 7. 实时通道

`/ws/alarm` 只推**只读**告警事件；dev 由 `websocket/AlarmSimulator` 每 12s 推一条模拟数据，生产改为基于 `fac_alarm` 表的增量事件驱动。包络固定为 `{topic:'alarm.push', payload:AlarmItem}` + 客户端 15s 心跳 `{type:'ping'}`（服务端静默吞掉）。禁止借 WS 通道下发任何控制指令。

## 8. Windows 工程注记

- 本机 Maven 唯一可用：`D:\apache-maven-3.9.11\...\mvn.cmd -s ci-settings.xml`（wrapper `mvnw` 与裸 `mvn` 均不可用）。
- 清理用 `mvn -s ci-settings.xml clean`，禁止 `rm -rf target`。
- 终止 Java 进程用真实 Windows PID + `taskkill /PID <winpid> /F /T`（Git-Bash 的 `kill <pid>` 杀不掉 Windows JVM）；验活看 `curl -s -m4 http://localhost:8787/actuator/health` 是否拒绝连接。
- Python 子进程在中文 Windows 上须显式 `encoding="utf-8", errors="replace"`，否则 GBK 解码静默丢数（冒烟一律走 Python utf-8）。

## 相关文档

- [domain-model.md](./domain-model.md) — 实体聚合与关系
- [filter-chain.md](./filter-chain.md) — 过滤器 / 拦截器顺序与鉴权细节
- [../database/README.md](../database/README.md) — Flyway 规约与逻辑删除
- [../test-strategy.md](../test-strategy.md) — 测试基线与覆盖率门禁
- [../glossary.md](../glossary.md) — 跨库术语表（与前端对齐）
- [../requirement/scope-inventory.md](../requirement/scope-inventory.md) — 交付范围追溯清单
