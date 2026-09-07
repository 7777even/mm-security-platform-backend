# docs/architecture/ — 后端架构与工程基线

> 长期共识文档：描述系统**现在**是什么样。改动代码后若下列任一项变化，必须回来同步本文件。

## 1. 技术栈与运行形态

| 项         | 值                                                                    |
| ---------- | --------------------------------------------------------------------- |
| 框架       | Spring Boot 3.2.5（Java 17）                                          |
| 持久层     | MyBatis-Plus 3.5.5（`map-underscore-to-camel-case`、逻辑删除 `deleted`） |
| 数据库     | PostgreSQL（生产）/ H2 内存库（dev 兜底）                              |
| 认证       | JJWT 0.12.5，无状态；access 2h、refresh 7d                             |
| 实时通道   | Spring WebSocket，`/ws/alarm` 告警推送                                 |
| 端口 / 前缀| `8080` / `/api/v1`                                                     |
| 构建       | Maven Wrapper `./mvnw`（禁止裸 `mvn`）                                 |

## 2. 分层与包职责

根包 `com.sinopec.mmsecurity`，九个包：

| 包            | 职责                                                 | 禁写                                                |
| ------------- | ---------------------------------------------------- | --------------------------------------------------- |
| `controller/` | 路由、参数绑定、`@Valid`、调用 Service               | 业务逻辑、自定义响应外壳、直接注入 Mapper           |
| `service/`    | 业务规则、事务边界                                   | `HttpServletRequest`、吞异常、自行放行硬控          |
| `mapper/`     | 数据访问（MyBatis-Plus）                             | 业务判断；自定义 SQL 需同步 schema 与 IT            |
| `entity/`     | 与表 1:1 的持久化对象                                | 直接作为出参                                        |
| `dto/`        | 出入参对象                                           | 复用 Entity 做出参、字段无中文注释                  |
| `config/`     | WebMvc / MybatisPlus / WebSocket / 安全 Bean 装配    | 业务规则                                            |
| `security/`   | JWT / HMAC / 硬控 / 鉴权 / UserContext               | 局部放宽（改动走 L4）                               |
| `common/`     | `Result` / `ResultCode` / `BusinessException` / `TraceContext` / `DeviceCode` | 局部改写包络与错误码            |
| `websocket/`  | 告警推送与模拟                                       | 借 WS 下发控制指令                                  |

## 3. 请求链路（顺序不可乱）

```
HTTP 请求
  ↓ HmacFilter           (1) 生产开启：校验 X-Timestamp / X-Nonce / X-Signature，容忍 300s
  ↓ JwtFilter            (2) 解析 Bearer Token，写入 UserContext
  ↓ HardControlInterceptor (3) 硬控路径 POST/PUT/DELETE → 503 HARD_CONTROL_BLOCKED
  ↓ RequireAuthInterceptor (4) @RequireAuth 鉴权
  ↓ Controller → Service → Mapper
  ↓ GlobalExceptionHandler → common/Result<T> 统一包络
```

新增过滤器必须在 `config/SecurityBeans` 显式声明顺序；**变更顺序属 L4**。

## 4. 安全基线与错误码

- 零下行控制：后端不提供下行控制写接口；`HardControlInterceptor.HARD_CONTROL_PATHS` 为唯一名单，新增名单外下行能力须人工评审。
- 密钥注入：`JWT_SECRET` / `DB_PASSWORD` / `SIGNATURE_SECRET` 只从环境变量取；`application*.yml` 仅保留占位默认值。
- 令牌：JWT 无状态，服务端不落盘、不写 Cookie；前端负责 HttpOnly Cookie / 内存态。
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

- 数据库版本化迁移由 **Flyway** 接管（不再用 `spring.sql.init` 加载 schema.sql/data.sql）。`spring.flyway.enabled=true`、`spring.sql.init.enabled=false` 在 `application.yml` 统一设定。
- 三套方言迁移脚本位于 `src/main/resources/db/migration/{h2,postgresql,dameng}/`，按 profile 由 `spring.flyway.locations` 指向：
  - `dev` → `h2`（H2 内存库，本地可实跑验证：V1 全量快照＋V2 种子）。
  - `prod` → `postgresql`（PostgreSQL，兼容/回退）。
  - `dm` → `dameng`（达梦 DM8，信创生产选定；DM 为 Oracle 兼容库，迁移按 Oracle 兼容方言编写，**Flyway 社区版无官方达梦 database 模块**，须到达梦实例复核）。
- `AuthService.ensureAdmin()` 在 Flyway 建好的 `sys_user` 上写入默认账号 `admin` / `admin@2026`（不进种子脚本，避免与业务初始化重复）。
- 双轨策略：完整快照（V1，空库直达最新）＋版本化增量 `V<yyyyMMddHHmmss>__<lower_snake_case>.sql`；已进入共享环境的 `V` 禁止修改 / 重命名 / 删除。后续任何 DDL 变更：①新增增量 V 文件；②proposal 写明存量影响与回退方案。
- 逻辑删除统一 `deleted`（`0` 未删 / `1` 已删），新表须带审计字段并在查询链路生效。

## 6. 环境与运行

```bash
# dev（H2 兜底，签名关闭）
./mvnw spring-boot:run

# 冒烟（health → login → dashboard → devices → 错密码 401）
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1

# prod（PostgreSQL + 签名开启）
SPRING_PROFILES_ACTIVE=prod DB_PASSWORD=... JWT_SECRET=... ./mvnw spring-boot:run
```

`application.yml` 关键开关：`jwt.access-ttl`（7200s）、`jwt.refresh-ttl`（604800s）、`signature.enabled`（默认 `false`，生产须 `true`）、`signature.max-skew-seconds`（300s）。

## 7. 实时通道

`/ws/alarm` 只推**只读**告警事件；dev 由 `websocket/AlarmSimulator` 每 12s 推一条模拟数据，生产改为基于 `fac_alarm` 表的增量事件驱动。禁止借 WS 通道下发任何控制指令。

## 8. Windows 工程注记

- 用 `./mvnw`（Git-Bash）或 `mvnw.cmd`（PowerShell / CMD）；禁止裸 `mvn`（版本漂移）。
- 清理用 `./mvnw clean`，禁止 `rm -rf target`。
- 终止 Java 进程用 `taskkill /PID <winpid> /F /T`；Git-Bash 的 `kill <pid>` 杀不掉 Windows JVM。验活看 `curl -s -m 4 http://localhost:8080/api/v1/health` 是否拒绝连接。
- PowerShell 脚本受执行策略限制时用 `powershell -ExecutionPolicy Bypass -File <script>`，不改全局策略。
