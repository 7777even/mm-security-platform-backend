# AGENTS.md — AI 编码必读（后端）

面向 AI 助手 / 自动化编码的项目级约束入口。**动手前先按 §1 判定改动等级（L0–L4）；L1 及以上在生成或修改接口、安全、持久化代码前，必须读完本文 §3 API 契约与 §6 既有项目约束，再动手。**

本库是**安全管控指挥系统后端服务**（`backend-scaffold`），与前端库 `frontend-scaffold` **平级双库、非 monorepo**。两端共用同一套 AI 规范骨架（分级 / 契约 / 记录闭环），但各有独立 `AGENTS.md`、独立 `openspec/`、独立 Git 提交 scope。跨库协作规则见 §11。

## 1. 分级工作流（L0–L4 决策树）

动手前先判定等级，并在回复中用一句话说明判定与理由。**分级只决定流程重量，不豁免 §3 API 契约、§6 红线与 §2 验证矩阵。**

### 1.1 决策树（按"是否改代码 → 是否改业务能力 → 是否高风险"逐级下沉）

- **不改代码**（解释 / 评审 / 状态汇报 / 只读检查 / 文本润色）→ **L0**：直接完成；不建文件、不起子 Agent、不调 openspec。
- **改代码但不改业务能力 / 接口契约 / 权限语义，且 L1 四门槛全满足** → **L1**：说明范围 → 直接改 → 跑最小验证（§2 矩阵对应行）→ 输出结果。
- **改代码但属依赖 / 构建 / 脚手架 / lint / 配置 / 非业务技术债，或 L1 门槛缺一** → **L2**：说明方案与影响 → 执行 → 跑受影响目标验证。
- **改业务能力**（接口能力 / 业务规则 / 状态流转 / 权限语义 / 数据模型）→ **L3**：openspec 提案 → 人工确认 → TDD 实施 → 验收 → 归档。
- **高风险**（契约语义 / 权限模型 / 数据库结构 / 安全过滤器链 / 部署与配置基线；硬门禁清单见 §8）→ **L4**：按 L3 执行，且实施前取得人工确认。

### 1.2 L1 四条门槛（缺一即升 L2 / L3）

① 不新增或改变业务能力、接口契约、权限语义、数据库结构；② 改动不超过 3 个文件；③ 目标明确、可逆，验证可在 5 分钟内完成；④ 不新增生产依赖。L1 / L2 禁止创建 openspec Change、计划文档或子 Agent。

### 1.3 规则优先级仲裁

规则冲突时从高到低执行，低阶规则不得覆盖高阶：

1. 平台安全策略与人工当场指令。
2. 本文档（含 §3 API 契约、§6 红线）。
3. 已确认的 `openspec/changes/<name>/` 与 `openspec/specs/`。
4. 当前 Change 的 `tasks.md` 中正在执行的 Task。
5. Skill / 插件自带的工作方法（含 superpowers）。

任何 skill 或插件不得绕过上级规则、自行扩大需求、新增平行任务源或改写既有契约。

### 1.4 前后端契约仲裁（跨库特例）

接口**机器可读契约的唯一真源在前端库** `frontend-scaffold/docs/api/*.openapi.json`（API Contract First，前端由它生成 TS 类型）。

- 后端**不得**在本库复制第二份 OpenAPI 主契约（禁止平行体系）；本库 `docs/api/` 只放实现映射与同步纪律（见 `docs/api/README.md`）。
- 后端改动对外接口时，必须在**同一次交付**内同步前端库契约文件，走 §11 跨库四同步。
- 契约与后端实现冲突时，**先改契约 + 人工确认，再改代码**；禁止"先改代码、契约后补"。

## 2. 最小验证矩阵 + 测试策略

### 2.1 最小验证矩阵

| 改动范围                                                    | 必跑验证                                                    |
| ----------------------------------------------------------- | ----------------------------------------------------------- |
| 文档、规范、AGENTS、注释                                    | `git diff --check`                                          |
| 单个 Controller / Service / DTO 的局部修改                  | `./mvnw -q compile`                                         |
| `common/`、`security/`、`config/` 等横切层                  | `./mvnw test`                                               |
| Entity / Mapper / `resources/db/*.sql` / 分页 / 逻辑删除     | `./mvnw test` + 启动后 `scripts/smoke-test.ps1`（见 §2.3）    |
| `pom.xml`、依赖、构建配置                                   | `./mvnw clean package -DskipTests`                          |
| 对外接口增删改（含字段 / 错误码 / 权限码）                  | `node scripts/check-api-contract.mjs` + `./mvnw test`       |
| L3 / L4                                                     | 按 `tasks.md` 验收标准全量，不得以 L1 / L2 降级             |

禁止为形式化验证在每次 L1 / L2 后连跑 compile + test + package 三套；只跑矩阵中对应的一行。

**环境注记（Windows）**：

- 一律用仓库自带 Maven Wrapper `./mvnw`（Git-Bash）或 `mvnw.cmd`（PowerShell / CMD）；**禁止**使用裸 `mvn` 造成版本漂移，也**禁止** `rm -rf target` 清理（用 `./mvnw clean`）。
- 终止 Java 进程用 Windows 真实 PID：`taskkill /PID <pid> /F /T`；在 Git-Bash 里 `kill <pid>` 杀不掉 Windows JVM（PID 命名空间不同）。验活以 `curl -s -m 4 http://localhost:8080/api/v1/health` 是否拒绝连接为准。
- PowerShell 执行脚本若报执行策略限制：用 `powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1`，不要改全局策略。

### 2.2 测试策略（起步基线）

本库已建立 `src/test/java` 测试基线（脚手架阶段起步），测试按以下顺序增量补齐，不照搬安全培训系统的 Testcontainers / 162+16 例规模：

1. **先补零依赖单测**（对应 `src/test/java`，类名 `*Test`）：
   - Controller：`MockMvcBuilders.standaloneSetup` + mock Service，验证路径与参数透传、分页绑定、`@Valid` 非法请求 400 且不进 Service。
   - Service：纯 Mockito 手动构造，覆盖成功路径 + 存在性 / 唯一性 / 状态 / 权限护栏，断言 `BusinessException` 的错误码与关键副作用。
   - 安全层：`JwtUtil` 签发/校验/过期、`HmacFilter` 签名字符串构造、`HardControlInterceptor` 硬控路径命中即拒。
2. **带 DB 的集成测试（`*IT`）已落地一层**：`src/test/java/.../integration/DbLayerIntegrationIT`（`@SpringBootTest` + `dev` profile）启动真实上下文，复用 `db/migration/h2` 的 **V1–V8 作为唯一 schema 来源（禁止在测试目录复制第二份 DDL）**，验证真实 SQL / 落库 / 逻辑删除（MyBatis-Plus 全局 `logic-delete-field`）/ 审计写入。Docker / Testcontainers 不可用，故以 H2 充当集成 DB；**生产库（达梦 / PG）语义不等价，最终必须在真实实例上复核**（见 `docs/deployment/dameng-migration-runbook.md`）。若后续引入 Testcontainers / PG 容器，可拓展 `*IT` 覆盖生产方言——但不得为「实跑不了」而用零 DB 通过冒充。
3. **先红后绿（TDD）**：新增行为 / 业务逻辑修改 / 缺陷修复必须先写失败测试；实现已存在则验其契约。代码完成后补写测试不得宣称为 TDD。
4. **回归闭环**：`./mvnw test` 为回归门禁，改动横切层（`common/` `security/` `config/`）时必须全绿。

### 2.3 冒烟验证（接口链路）

```bash
./mvnw spring-boot:run                                  # 启动（dev + H2，无需本地 PostgreSQL）
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

冒烟覆盖：health → login → 带 token 取 dashboard → 带 token 分页取 devices → 错误密码期望 401。任一环失败即视为未通过，不得声称交付完成。

## 3. API 契约规则

（对齐前端 `frontend-scaffold/AGENTS.md` §3，后端为实现方；机器可读真源见 §1.4）

1. **零下行控制红线**：后端**不提供**任何下行控制写接口。`security/HardControlInterceptor.HARD_CONTROL_PATHS` 名单内的 POST / PUT / DELETE 一律以 `code=503 HARD_CONTROL_BLOCKED` 拒绝；新增下行能力只能在名单外路径，且必须经人工评审确认不属于下行控制。名单变更属 **L4**。
2. **B3 统一响应包络**：所有业务响应统一 `common/Result<T>`（`code` / `message` / `data` / `traceId`），`code=0` 为成功；非 0 由 `GlobalExceptionHandler` 统一转包络。**禁止** Controller 自定义第二套响应外壳或裸返实体。
3. **20 位中石化 MDM 设备编码**：设备物理主键固定 20 位 MDM 编码，禁止自创物理主键（自增 id / uuid / 序号作对外标识）。路径 / 查询参数 / body 中的设备标识一律用 20 位编码，并用 `@DeviceCode` 注解校验（违反返回 `code=301 DEVICE_CODE_INVALID`）。
4. **防重放签名**：生产环境开启 `signature.enabled=true`，`security/HmacFilter` 强制校验 `X-Timestamp` / `X-Nonce` / `X-Signature`（HMAC-SHA256，payload = `ts\n nonce\n method\n uri`）。Dev 通过 `signature.enabled=false` 挂起，但**禁止**在生产旁路签名或把签名开关写死为关闭。
5. **令牌内存态**：JWT 无状态，服务端**不落盘**、不写 Cookie；前端负责 HttpOnly Cookie / 内存态存储。后端只签发 access（`jwt.access-ttl`，默认 2h）与 refresh（默认 7d），`jwt.secret` 必须走环境变量，禁止硬编码进 `application.yml` 提交入库。
6. **分层与目录**：`controller` / `service` / `mapper` / `entity` / `dto` / `config` / `security` / `common` / `websocket` 九包，根包 `com.sinopec.mmsecurity`；业务模块作为第一级再细分。Controller 不写业务逻辑，Service 不感知 HTTP，Mapper 不写业务判断。

### 3.1 错误码分段（新增码必须落在所属段）

| 段     | 域         | 现有码                                                             |
| ------ | ---------- | ------------------------------------------------------------------ |
| `1xx`  | 通用       | `100` 参数非法、`401` 未认证、`403` 无权限、`404` 资源不存在        |
| `2xx`  | 鉴权域     | `201` TOKEN_EXPIRED、`202` TOKEN_INVALID、`203` SIGNATURE_INVALID、`204` SIGNATURE_EXPIRED |
| `3xx`  | 设备域     | `301` DEVICE_CODE_INVALID、`302` DEVICE_NOT_FOUND                   |
| `5xx`  | 硬控域     | `503` HARD_CONTROL_BLOCKED（下行控制被拒）                          |

新增错误码必须同步：①`common/ResultCode` 常量；②前端库对应 `<domain>.openapi.json` 的错误响应；③调用方处理分支。

## 4. 工程记录闭环

实施任务的唯一真源是 `openspec/changes/<name>/tasks.md`。

- 会话内进度跟踪只作临时备忘，不写入仓库；任务状态只回填 `tasks.md` 勾选框。
- 禁止在 `openspec/` 之外建立第二套需求规格或任务清单（含 skill 生成的计划文件、持久化待办）。
- superpowers 定位为**可组合的工程辅助，而非常驻流程**：沿用其 `test-driven-development`、`systematic-debugging`、`verification-before-completion` 的方法要求；其 `brainstorming`、`writing-plans`、子 Agent 调度与第二套 Review，在已确认 Change 的 `proposal.md` / `tasks.md` 已覆盖同一职责时不得重复启用。
- L0 / L1 / L2 不默认起子 Agent、不写计划文档；仅在任务复杂度或用户要求达到阈值时升级为 L3。

三类目录职责不重叠：

| 目录           | 回答的问题           | 特征                                 |
| -------------- | -------------------- | ------------------------------------ |
| `docs/`        | 系统**现在**是什么样 | 长期共识，跨版本有效，改了要同步代码 |
| `openspec/`    | 系统**将要**怎么变   | 唯一业务规格来源                     |
| `engineering/` | 这次**做得怎么样**   | 短期过程记录：计划、QA、复盘         |

- 短期开发记录放 `engineering/`，**不放 `docs/`**；QA 结果、发布检查、复盘同理。
- 详见 `docs/AGENTS.md` 与 `engineering/README.md`。

## 5. 完成标准（Definition of Done）

任一 L3 / L4 改动在声称完成前，必须满足：

1. **验收标准达成**：`openspec/changes/<name>/tasks.md` 全部勾选，验收标准逐条满足。
2. **回归全绿**：按 §2 矩阵对应行执行，`./mvnw test` 0 failure、接口链路冒烟通过。
3. **文档同步**：代码改动若改变契约 / 行为 / 数据结构，同步更新 `docs/` 与**前端库** `docs/api/*.openapi.json`（§11 四同步）；禁止把短期记录写进 `docs/`。
4. **提交按 scope 拆分**：`type(scope): 描述`（conventional commits + 后端 scope），跨影响面拆多提交，提交信息单行成句、禁止分点列表；禁止提交临时输出文件（如 `tsc-out.txt`、`mvn-out.txt`、`nohup.out`）。

## 6. 既有项目约束

### 6.1 项目概览

安全管控指挥系统后端服务：Spring Boot 3.2.5 + Java 17 + MyBatis-Plus 3.5.5 + PostgreSQL（生产）/ H2（dev 兜底）+ JJWT 0.12.5 + WebSocket。

- 端口：base `application.yml` 为 `8080`，dev profile（`application-dev.yml`）对齐前端 `VITE_API_BASE` 改为 `8787`（前端 dev 关 mock 后直连，零前端改动）。REST 前缀 `/api/v1`；WS 端点 `/ws/alarm`（告警实时推送，包络见 `docs/integration/README.md` 与 `frontend-scaffold/docs/api/realtime.openapi.json`：`{topic:'alarm.push', payload:AlarmItem}`）。
- 过滤器 / 拦截器顺序（Servlet 级，先于 DispatcherServlet）：`CorsFilter`(HIGHEST_PRECEDENCE，先给所有响应加 CORS 头) → `HmacFilter`(HIGHEST_PRECEDENCE+1) → `JwtFilter`(HIGHEST_PRECEDENCE+10) → `HardControlInterceptor`(3) → `RequireAuthInterceptor`(4)。CORS 必须在最前：否则被 JwtFilter 短路的 401 响应无 CORS 头，浏览器报「No 'Access-Control-Allow-Origin' header」。前后端联调运行手册见 `docs/integration/README.md`。
- dev profile 走 H2 内存库（Flyway 迁移 `db/migration/h2` 自动建表＋种子，不再用 `schema.sql`/`data.sql`），启动即由 `AuthService.ensureAdmin()` 写入默认账号 `admin` / `admin@2026`。

### 6.2 目录职责与红线

| 包 / 目录                 | 职责                                                   | 红线                                                       |
| ------------------------- | ------------------------------------------------------ | ---------------------------------------------------------- |
| `controller/`             | 路由、参数绑定、`@Valid`、调用 Service                 | 不写业务逻辑；不自定义响应外壳；不直接注入 Mapper           |
| `service/`                | 业务规则、事务边界                                     | 不感知 `HttpServletRequest`；不吞异常；不自行放行硬控       |
| `mapper/`                 | MyBatis-Plus 数据访问                                  | 不写业务判断；自定义 SQL 需同步 schema 与 IT               |
| `entity/`                 | 与表结构 1:1 的持久化对象                              | 改动必须同步 `resources/db/*.sql`（§6.4）                  |
| `dto/`                    | 出入参对象                                             | 每个字段必须有中文注释 / `@Schema`；不复用 Entity 做出参    |
| `config/`                 | WebMvc / MybatisPlus / WebSocket / 安全 Bean 装配      | 不写业务规则；新过滤器须显式声明顺序                        |
| `security/`               | JWT / HMAC / 硬控 / 鉴权注解与拦截器 / UserContext     | 属 L4 门禁区，改动前须人工确认（§8）                       |
| `common/`                 | `Result` / `ResultCode` / `BusinessException` / `TraceContext` / `DeviceCode` | 包络与错误码是跨端契约，禁止局部改写        |
| `websocket/`              | 告警推送与模拟器                                       | 只推只读事件，禁止借 WS 通道下发控制指令                    |
| `resources/`              | `application*.yml`、`db/migration/**`                  | 密钥一律 `${ENV:默认值}`，禁止提交真实生产口令；V 迁移文件禁止改/删 |

### 6.3 安全红线

1. 零下行控制（§3.1）；WebSocket 通道同样不得下发控制指令。
2. 密钥 / 口令 / 签名 secret 只从环境变量注入（`JWT_SECRET`、`DB_PASSWORD`、`SIGNATURE_SECRET`），默认值为占位串，生产部署必须覆盖。
3. 未登录默认拒绝：除下述免鉴权白名单外，新端点默认需鉴权；新增免鉴权端点须在 proposal 中显式说明理由。当前白名单（`JwtFilter.WHITELIST`）：`/api/v1/auth/login`、`/api/v1/auth/refresh`、`/api/v1/auth/menus`、`/api/v1/auth/me`、`/api/v1/health`、`/actuator`、`/h2-console`、`/ws`、`/error`（OPTIONS 预检一律放行）。鉴权失败 `JwtFilter` 直接写 HTTP 401 + B3 包络（不抛异常冒泡成 500）；前端 `main.ts` 在 401 时清内存令牌并跳登录（见 `docs/integration/README.md`）。
4. 日志脱敏：禁止打印令牌、口令、签名头、完整请求体敏感字段；`traceId` 由 `TraceContext` 透传，前后端联调以它对齐。
5. SQL 注入：禁止字符串拼接 SQL；MyBatis-Plus 条件构造器优先，`${}` 一律禁止。

### 6.4 数据库变更规则

1. **禁止直接操作生产数据库**；任何结构变更属 **L3**，索引 / 字段类型 / 约束变更属 **L4**。
2. **数据库版本化迁移由 Flyway 接管**（`application.yml` 中 `spring.flyway.enabled=true`、`spring.sql.init.enabled=false`）；不再维护 `schema.sql`/`data.sql` 快照。三套方言迁移位于 `src/main/resources/db/migration/{h2,postgresql,dameng}/`，由 `spring.flyway.locations` 按 profile 指向（dev→h2、prod→postgresql、dm→dameng）。
3. **双轨策略**：`V1__init_schema.sql` 为完整快照（空库直达最新结构），后续 DDL 一律新增版本化增量 `V<yyyyMMddHHmmss>__<lower_snake_case>.sql`；已进入共享环境的 `V` 文件**禁止修改、重命名或删除**。
4. 任何结构变更须：①新增增量 V 文件（不得改 V1）；②在 proposal 中写明对存量数据的影响与回退方案；③若 dev 快照（h2 V1）涉及结构变化，同步更新 `postgresql/`、`dameng/` 两套方言的同版本/对应增量文件，保持三库列定义一致。
5. 逻辑删除统一 `deleted` 字段（`0` 未删 / `1` 已删，`application.yml` 全局配置）；新表必须带该字段与审计字段，并在 Mapper 查询链路生效。
6. 达梦 DM8 为信创生产选定库（Oracle 兼容）；**Flyway 社区版无官方达梦 database 模块**，达梦迁移脚本按 Oracle 兼容方言编写，必须在达梦实例上复核执行（PG / H2 迁移则可由本地 Flyway 实跑验证）。

### 6.5 工程约定（Git / 提交）

- **提交格式 `type(scope): 描述`**，scope 固定枚举、禁止自造：
  `auth`（认证域）、`device`（设备域）、`alarm`（告警域 + WebSocket 推送）、`dashboard`（态势总览）、`security`（JWT / HMAC / 硬控 / 鉴权横切）、`common`（Result / 异常 / DeviceCode / 工具）、`db`（schema 与迁移脚本）、`config`（配置类、`application*.yml`、构建）、`docs`、`chore`。
- 跨域改动**按影响面拆成多个提交**：横切层（`common` / `security`）先行，业务域跟随；确属原子改动才允许双 scope（如 `feat(alarm,security):`），不得常态化。
- 提交信息**只写一句总结性语句**，禁止长段落或 `- ` 分点列表；禁止提交构建产物与临时输出（`target/` 已被 `.gitignore` 覆盖）。
- Java 代码统一 **4 空格缩进**、UTF-8、必要处写中文注释；新增对外接口必须同步契约（§11）。

### 6.6 分层规则与读取链

| 目标目录                          | 读取链                                                     |
| --------------------------------- | ---------------------------------------------------------- |
| `controller/` `service/` `dto/`   | 根 §1–§6 → `docs/api/README.md` → 前端库 `<domain>.openapi.json` |
| `security/` `config/`             | 根 §1–§6 → `docs/architecture/README.md`                   |
| `mapper/` `entity/` `resources/`  | 根 §1–§6 → `docs/architecture/README.md`（数据层与迁移）    |
| `docs/`                           | `docs/AGENTS.md`                                           |

分层文件与本文件冲突时按 §1.3 仲裁：根 `AGENTS.md` 高于分层，分层只能加严、不得放宽。

## 7. L3 / L4 四件套、QA/Retro 即刻记录与模板体系

### 7.1 L3 / L4 强制 OpenSpec 四件套

L3 / L4 改动动手前必须完成并闭环以下四件套（位于 `openspec/changes/<name>/`），且经末尾「人工确认关卡」确认后才允许写代码：

- `proposal.md`（Why / What / Capabilities / Impact + 人工确认关卡）
- `design.md`（架构、决策 ADR、风险、依赖、数据影响）
- `tasks.md`（≤2h 可勾选任务，[TDD] 先写失败测试；任务状态只回填此处）
- `spec-delta.md`（新增 / 修改 / 移除 三段，与 `spec.md` 同构）

四者须闭环：`proposal` 的 Capabilities ↔ `spec-delta` 的 Requirement ↔ `tasks` 的验收标准一一对应。禁止 L1 / L2 建立 OpenSpec Change。

**归档闭环（全勾必归档）**：`tasks.md` 全部勾选后，必须在**同一次交付内**完成收尾，不允许滞留 `changes/`：

1. **spec 回填**：将 `spec-delta.md` 合入 `openspec/specs/<capability>/spec.md`（新建或扩充 capability，Requirement/Scenario 格式）。
2. **归档**：`git mv openspec/changes/<name> openspec/archive/<YYYY-MM-DD>-<name>`（日期前缀必带）。
3. **守门**：CI 跑 `node scripts/check-openspec-hygiene.mjs`（全勾未归档 / 归档缺日期前缀即失败；四件套缺失与进行中 Change 命名前缀为告警——存量 Change 允许只有 proposal+tasks，新提案必须齐全）；纪律细则见 `openspec/changes/README.md`。
4. **命名与元数据**：进行中 Change 也建议带 `YYYY-MM-DD-` 前缀，且每个 Change 含 `.openspec.yaml`（`schema: spec-driven` + `created: <YYYY-MM-DD>`）；hygiene 会对其告警提示，确保归档时前缀一致。

### 7.2 QA / Retro 即刻记录

L3 / L4 任务完成后**即刻**写 `engineering/qa/` 与 `engineering/retro/`，不允许攒到最后补；L0–L2 不写。

- QA：范围、验收口径、实际执行命令与用例数、未运行项、结论；**证据是结论必要附件**（接口用例附 curl / 冒烟终端输出快照，置于同目录引用文件名）。
- Retro：做得好 / 问题 / 原因 / 改进方案四段式。

### 7.3 模板体系（位于 `templates/`）

| 模板                                         | 用途                   |
| -------------------------------------------- | ---------------------- |
| `templates/_openspec-proposal_template.md`   | 四件套 · proposal      |
| `templates/_openspec-design_template.md`     | 四件套 · design        |
| `templates/_openspec-tasks_template.md`      | 四件套 · tasks         |
| `templates/_openspec-spec-delta_template.md` | 四件套 · spec-delta    |
| `templates/_qa_template.md`                  | engineering/qa 记录    |
| `templates/_retro_template.md`               | engineering/retro 记录 |
| `templates/_ship_template.md`                | engineering/ship 发布检查与回滚 |
| `templates/api-contract-writing-guide.md`    | §3 契约编写与同步手册  |

`templates/README.md` 为索引与用法说明。

## 8. L4 硬门禁清单

以下任一类改动命中即升 **L4**（按 §1.1 取得人工确认后才实施），不得按 L1 / L2 直接动手：

- **契约语义**：B3 包络结构、`ResultCode` 错误码分段与语义、`/api/v1` 前缀变更。
- **权限与认证**：`JwtFilter` / `RequireAuthInterceptor` / `@RequireAuth` 语义、免鉴权白名单、角色与权限模型。
- **防重放与硬控**：`HmacFilter` 签名算法与容忍窗口、`HardControlInterceptor.HARD_CONTROL_PATHS` 名单。
- **数据层**：表 / 字段 / 索引 / 约束变更、逻辑删除与审计字段语义、分页与乐观锁行为。
- **横切配置**：`application*.yml` 生产基线、`SecurityBeans` 过滤器顺序、WebSocket 端点与推送协议。
- **依赖与框架**：生产依赖新增、Spring Boot / MyBatis-Plus 版本升级、引入 Flyway / Testcontainers 等基础设施。

## 9. Review 结论三选一

任何代码评审 / 变更评审的结论必须为以下三者之一，**禁止含糊带过**：

- **通过**：无需修改，可直接合入。
- **需修改**：明确指出修改点，修改后无需再全员评审。
- **需人工决策**：存在高风险 / 规范冲突 / 范围扩散等需拍板事项，转交人工确认（对应 L4 关卡）。

> 不得出现「基本可以」「再看下」「问题不大」等模糊结论。L3 / L4 另需**双轴 Review**：①规格符合性（是否严格实现已确认 Task、契约与验收标准）；②代码质量（越界改动、错误边界、测试遗漏、无关重构、无用依赖）。

## 10. 测试与长期约束

- 测试策略基线见 §2.2：**先补零依赖单测，再谈集成测试**；不照搬安全培训系统的全量金字塔规模。
- 坚持**先红后绿（TDD）** + **回归闭环**（`./mvnw test` 为门禁）。
- 接口链路改动必须以 `scripts/smoke-test.ps1` 通过为准；涉及对外接口时先跑 `node scripts/check-api-contract.mjs`。

## 11. 跨库协作（后端 ⇄ 前端）

### 11.1 职责边界

| 事项             | 后端（本库）                                  | 前端（`frontend-scaffold`）                    |
| ---------------- | --------------------------------------------- | ---------------------------------------------- |
| 契约真源         | **实现方**，以契约为准实现                    | `docs/api/*.openapi.json` 单一真源，生成 TS 类型 |
| 硬控红线         | `HardControlInterceptor` 服务端兜底拒绝       | http 拦截器 `guardHardControl` 出站拦截        |
| 令牌             | 签发 JWT，服务端不落盘                        | HttpOnly Cookie / 内存态存储与注入             |
| 签名             | `HmacFilter` 校验三头                         | http 拦截器生成三头（生产强制）                |
| 数据权限         | 服务端按用户 / 组织过滤，返回已过滤结果       | `v-permission` 只管显隐，不做数据过滤          |

### 11.2 契约变更四同步（不可跳步）

1. **OpenSpec**：后端业务变更先走 `opsx-*` / `.cursor/skills/openspec-*`，落地 `openspec/changes/<name>/`。
2. **前端契约**：同一次交付内更新 `frontend-scaffold/docs/api/<domain>.openapi.json`（四条铁律：按域分组 / 接口有注释 / 字段有中文 description / 有 example）。
3. **后端实现**：Controller / DTO 与契约对齐，跑 `node scripts/check-api-contract.mjs` 校验端点不漂移。
4. **前端类型**：前端重跑 `npm run gen:api-types` 并调整调用方；后端在 QA 记录中注明"已通知前端重生成类型"。

禁止：只改后端代码不改前端契约；只改契约不通知前端重生成；用 Markdown 接口清单替代 OpenAPI。

### 11.3 禁止事项（AI 权限边界）

AI 负责：需求分析、技术设计、编码、测试、Review、文档。
人工负责：需求确认、架构决策、权限确认、数据库结构确认、最终合并。

AI **禁止**：操作生产环境、修改生产数据、自造业务规则、自建权限模型、未经批准改数据库结构、未确认引入依赖、在后端库内新建第二套契约或任务体系。
