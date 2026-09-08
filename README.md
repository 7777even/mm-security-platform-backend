# mm-security-backend

安全管控指挥系统 — 后端服务。Spring Boot 3.2 + MyBatis-Plus + PostgreSQL + JWT。

与前端的对接北向约定：遵循前端脚手架 `frontend-scaffold/AGENTS.md` §3 API 契约。

## 0. AI 协作入口（动手前必读）

> 跨工具通用速查（分级速记 / 验证矩阵 / 红线 / 机器消费闭环）见前端库 `frontend-scaffold/docs/ai-collaboration-guide.md`。

本库与前端库 `frontend-scaffold` 为**平级双库、非 monorepo**，两端共用同一套 AI 规范骨架：

| 文件 / 目录                       | 作用                                                             |
| --------------------------------- | ---------------------------------------------------------------- |
| `AGENTS.md`                       | **AI 编码入口**：L0–L4 分级、验证矩阵、API 契约、红线、跨库协作  |
| `CLAUDE.md`                       | Claude Code 侧的入口投影（优先级与红线速览）                      |
| `.cursor/rules/*.mdc`             | Cursor / 各 AI 工具的核心约束镜像                                 |
| `.cursor/commands/opsx-*.md`      | OpenSpec 六步命令（propose/apply/archive/explore/sync/update）    |
| `.cursor/skills/openspec-*/`      | 同上流程的技能版（意图匹配自动触发）                              |
| `openspec/`                       | 唯一业务规格来源（changes / specs / archive）                     |
| `docs/`                           | 长期共识（架构、跨库契约同步纪律）                                |
| `engineering/`                    | 短期过程记录（plans / qa / retro）                                |
| `templates/`                      | 四件套 + QA/Retro + 契约编写指南模板                              |

三条最容易踩的红线：

1. **接口契约真源在前端库** `frontend-scaffold/docs/api/*.openapi.json`——本库**不复制第二份**；改接口必须同交付走跨库四同步。
2. **零下行控制**——后端不提供下行控制写接口，`HardControlInterceptor` 名单内写操作一律 `503`。
3. **对外响应统一 `Result<T>` 包络**——`code=0` 才取 `data`，禁止自定义第二套响应外壳。

改接口前跑一次端点比对：`node scripts/check-api-contract.mjs`。

## 1. 关键契约对齐

| 契约 | 实现 |
|---|---|
| B3 统一响应包络 | `common/Result.java`：code=0 返回 data，非 0 抛业务错误 |
| 令牌内存态 | JWT 无状态、服务端不落盘；前端 HttpOnly Cookie / memory |
| 20 位 MDM 设备编码 | `common/DeviceCode` 校验注解（路径参数 `@DeviceCode`） |
| 零下行控制 | `security/HardControlInterceptor` 兜底拒绝 POST/PUT/DELETE 硬控路径 |
| 防重放签名（生产） | `security/HmacFilter`：X-Timestamp / X-Nonce / X-Signature |
| 目录与分层 | controller / service / mapper / entity / dto / config / security / common / websocket |

## 2. 工程约定

- 端口：`8080`
- REST 前缀：`/api/v1`
- WS 端点：`/ws/alarm`（告警实时推送）
- Maven Wrapper：`./mvnw`（严禁 `rm -rf` 与 `mvn` 路径混淆）
- 提交规范：`type(scope): 描述`，scope 固定枚举 `auth/device/alarm/dashboard/security/common/db/config/docs/chore`（详见 `AGENTS.md` §6.5）
- 契约校验：`node scripts/check-api-contract.mjs`（比对后端 Controller 端点与前端 `docs/api/*.openapi.json`）

```bash
# 构建
./mvnw clean package -DskipTests

# 启动（默认 dev 环境，H2 兜底，无需本地 PostgreSQL；Flyway 自动建表+种子）
./mvnw spring-boot:run

# 生产（PostgreSQL，兼容/回退）
SPRING_PROFILES_ACTIVE=prod DB_PASSWORD=... JWT_SECRET=... ./mvnw spring-boot:run

# 生产（达梦 DM8，信创选定；需先 install 达梦驱动，见 application-dm.yml）
SPRING_PROFILES_ACTIVE=dm DB_PASSWORD=... JWT_SECRET=... SIGNATURE_SECRET=... ./mvnw spring-boot:run -Pdm
```

## 3. 环境配置

`src/main/resources/application.yml` 通过 `spring.profiles.active` 切换：

| Profile | 数据库 | 签名校验 | Flyway 迁移位置 |
|---|---|---|---|
| `dev` | H2 内存库 | 关闭（`signature.enabled=false`） | `classpath:db/migration/h2` |
| `prod` | PostgreSQL（兼容/回退） | 开启（`signature.enabled=true`） | `classpath:db/migration/postgresql` |
| `dm` | 达梦 DM8（信创生产选定） | 开启（`signature.enabled=true`） | `classpath:db/migration/dameng` |

> 数据库版本化迁移由 **Flyway** 接管（不再用 `spring.sql.init` 加载 schema.sql/data.sql）。
> 双轨策略：V1 全量快照（空库直达最新）＋ `V<yyyyMMddHHmmss>__<snake>.sql` 增量；已进入共享环境的 V 文件禁止修改/重命名/删除。
> 三套方言迁移脚本位于 `src/main/resources/db/migration/{h2,postgresql,dameng}/`。

### 3.1 生产数据库连接（必须设置环境变量）

PostgreSQL（`prod`）：

```yaml
spring.datasource.url=jdbc:postgresql://localhost:5432/mm_security
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
```

达梦 DM8（`dm`，信创生产选定），启用前需先本地安装驱动（见 `application-dm.yml` 注释）：

```yaml
spring.datasource.url=jdbc:dm://localhost:5236/mm_security
spring.datasource.username=${DB_USERNAME:SYSDBA}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=dm.jdbc.driver.DmDriver
```

## 4. 接口清单

### 4.1 认证域（免鉴权）

| 方法 | 路径 | 用途 |
|---|---|---|
| GET  | `/api/v1/health` | 健康检查 |
| POST | `/api/v1/auth/login` | 登录，返回 access + refresh token |
| POST | `/api/v1/auth/refresh` | 用 refresh 换新 access |
| GET  | `/api/v1/auth/me` | 当前用户信息 |
| GET  | `/api/v1/auth/menus` | 菜单树 |

默认账号：`admin` / `admin@2026`（dev 环境启动时 `AuthService.ensureAdmin()` 自动写库）。

### 4.2 业务域（需 Authorization: Bearer <token>）

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/api/v1/dashboard/overview` | 设施概览 KPI |
| GET | `/api/v1/dashboard/workstations` | 值班站点 |
| GET | `/api/v1/devices` | 设备分页（支持 status / zone / deviceCode 筛选） |
| GET | `/api/v1/devices/{code}` | 设备详情（code 必须 20 位 MDM 编码） |
| GET | `/api/v1/alarms` | 告警分页（支持 level / status 筛选） |

### 4.3 WebSocket

ws://localhost:8080/ws/alarm — dev 每 12s 推送一条模拟告警。生产改为基于 `fac_alarm` 表的增量事件驱动。

## 5. 安全层

| 过滤器/拦截器 | 顺序 | 用途 |
|---|---|---|
| `HmacFilter` | 1（生产） | HMAC-SHA256 防重放（dev 挂起） |
| `JwtFilter` | 2 | JWT 校验，写入 UserContext |
| `HardControlInterceptor` | 3 | 零下行控制兜底 |
| `RequireAuthInterceptor` | 4 | @RequireAuth 鉴权 |

新增硬控路径：编辑 `HardControlInterceptor.HARD_CONTROL_PATHS`。

## 6. 验证

```bash
./mvnw compile                  # 编译
./mvnw spring-boot:run          # 启动（Tomcat 8080）
curl http://localhost:8080/api/v1/health
```

## 7. 目录树

```
backend-scaffold/
├── pom.xml                       # Spring Boot 3.2.5 + MyBatis-Plus + JJWT
├── src/main/
│   ├── java/com/sinopec/mmsecurity/
│   │   ├── Application.java      # 入口（@EnableScheduling + @MapperScan）
│   │   ├── common/               # Result / TraceContext / BusinessException / DeviceCode
│   │   ├── config/               # WebMvc / MybatisPlus / WebSocket / SecurityBeans
│   │   ├── controller/           # Auth / Health / Dashboard / Device / Alarm
│   │   ├── service/              # AuthService / DashboardService / DeviceService / AlarmService
│   │   ├── mapper/               # SysUser / FacDevice / Alarm
│   │   ├── entity/               # SysUser / SysMenu / FacDevice / FacAlarm
│   │   ├── dto/                  # LoginRequest / TokenResponse
│   │   ├── security/             # JwtUtil / JwtFilter / UserContext / RequireAuth / HardControl / Hmac
│   │   └── websocket/            # AlarmWebSocketHandler / AlarmSimulator
│   └── resources/
│       ├── application.yml       # 主配置（Flyway 启用、sql.init 关闭）
│       ├── application-dev.yml   # dev（H2 兜底，Flyway 位置 = h2）
│       ├── application-prod.yml  # PostgreSQL（Flyway 位置 = postgresql）
│       ├── application-dm.yml    # 达梦 DM8（Flyway 位置 = dameng，信创生产选定）
│       └── db/migration/
│           ├── h2/               # V1 全量快照 + V2 种子（H2 方言，本地可验证）
│           ├── postgresql/       # V1 + V2（PG 方言，兼容/回退）
│           └── dameng/           # V1 + V2（DM8 Oracle 兼容方言，需实例验证）
└── .mvn/wrapper/                 # Maven Wrapper（mvnw）
```
