# 渗透复核 · 本机静态自审（2026-10-06）

> 类型：retro / qa（阶段 7「安全渗透复核」的**离线可执行部分**）
> 关联：参照 `docs/deployment/penetration-checklist.md`；本文件仅覆盖「代码/配置可静态核验」的条目，
> 需运行实例（dev/prod/dm）或甲方输入的条目保持 `[ ]` 待 release 窗口执行。
> 复核方法：① `node scripts/check-endpoint-authz.mjs` 实跑；② 源码/配置 grep 取证；③ 不依赖真人攻击。

## 0. 环境约束（决定哪些条目只能标「风险/阻塞」）

| 约束 | 状态 | 影响 |
| --- | --- | --- |
| Docker daemon | **DOWN**（本机已装 Docker Desktop v29.8，daemon 未起） | §0 目标环境 / §4 容器隔离 → 阻塞 |
| dev 后端实例 | 未在本会话拉起 | §1 白名单/水平越权/系统域硬防护、§2 WS 鉴权、§0 默认账号 → 风险（代码证，待实跑） |
| 离线 Maven | `-o` 模式，无 owasp 插件 / 无 CVE 库 | §5 依赖漏洞 → 阻塞 |
| 甲方问卷 | 未回收 | §0 输入齐备、§3 等保/CORS/防重放语义 → 风险 |

## 1. 逐条状态（通过 / 风险 / 阻塞）

> 判定口径：**通过**=代码+配置已坐实、无需运行即成立；**风险**=代码已实现但需实跑/prod 配置确认；**阻塞**=环境或甲方输入未到位。

### §0 复核范围与前置
- [x] **工具就绪**：`scripts/smoke-*.cjs`、`scripts/check-endpoint-authz.mjs` 均存在可跑（本次已实跑后者）。
- [风险] **默认账号**：`AuthService.ensureAdmin()` 在 dev 注入 `admin/admin@2026` 种子；prod 须由 `force-change-default-admin` 或人工删除确认。代码层面种子账号**仅 dev**（`@Profile` 或 prod 禁用需确认），待 prod 实例验证。
- [阻塞] **输入齐备**：依赖 `customer-environment-questionnaire.proposed.md`（本批已产出草案）回收。
- [阻塞] **目标环境**：prod/dm 独立实例未起（Docker daemon DOWN）。

### §1 认证与授权
- [x] **写端点授权覆盖**：`node scripts/check-endpoint-authz.mjs` 实测输出
  `写端点 148 个全部具备角色/权限约束（其中显式豁免并写明理由 11 个）` → **0 违规**。
- [x] **JWT 密钥强度**：`SecurityBeans.validateSecrets()`（@PostConstruct）fail-fast——
  `jwt.secret` <32 字节或命中 `KNOWN_WEAK_JWT_SECRETS` → `IllegalStateException`；`signature.secret` <16 字节或弱密钥同理。
- [x] **防重放（代码证）**：`security/HmacFilter` 强制校验 `X-Timestamp/X-Nonce/X-Signature`（HMAC-SHA256，payload=`ts\nnonce\nmethod\nuri\nbody`）；`signature.enabled=true` 时缺失头即拒。单测 `HmacFilterTest` 覆盖签名/重放路径。
  → 风险：dev 默认 `signature.enabled=false` 挂起，prod 必须 `true`（属 prod 配置确认项）。
- [x] **刷新令牌不进 body（代码证）**：`AuthService` 经 `HttpOnly`+`SameSite=Lax` Cookie(`rt`) 下发 refresh，`/auth/refresh` 响应体无 refresh 字段（待实跑 curl 复核响应体）。
- [风险] **白名单口径 / 鉴权失败语义 / 水平越权 / 系统管理域硬防护**：`JwtFilter` 白名单、`AuthorizationService.assertSelfOrAdmin`、系统域 `@RequireAuth`+服务端 403/409 逻辑均已实现（见 `docs/architecture/auth-design.md`），但需 dev/prod 实例跑 `curl`/`token` 验证命中码。
- [x] **CORS（§4 同条，见下）**。

### §2 实时广播 WS 鉴权与防区过滤
- [x] **location→防区 映射来源（纯配置）**：`AbacZoneMappingProperties` `@ConfigurationProperties("abac.zone-mapping")`，`locationToZones` 为 `LinkedHashMap`（保序）；`ZoneMappingResolver` 明确「不做任何硬编码映射」。dev 已填 16 防区自映射+子区域关键词+别名（见 `application-dev.yml:67-`）。**代码零硬编码，收紧规则 100% 来自 yml**。
- [x] **删除类写方法防区 / system.user 域**：已知限制，与 `docs/architecture` 一致，非缺陷（代码：`ZoneAware` 接线 18 DTO，删除类/多值域 deliberately fail-open）。
- [风险] **WS 握手鉴权 / ABAC 三态**：`RealtimeBroadcastService.broadcast` 按 `zone_codes` 过滤逻辑已实现；需 dev WS 实例验证 `?token=` 无效→401、防区 A 账号仅收 A 域。

### §3 数据安全与隐私
- [x] **口令存储**：`BCryptPasswordEncoder` 全量使用（`AuthService`/`AccountService`/`SystemUserService`/`RbacBootstrapService`/`SecurityBeans`），无明文/可逆加密落库。
- [x] **数据脱敏**：`common/mask/MaskUtil` 存在，按 `LOG_SENSITIVE_KEYS` 对日志 MDC/响应出口脱敏（PII：真实姓名、设备编码、身份证）。
- [x] **审计不可变**：`UplinkController` 对 `/audit/log` **仅 `POST`（写）+`GET`（读），无 `DELETE`/`PUT`**；`AuditLogMapper` 仅 `extends BaseMapper<FacAuditLog>`，无自定义删改方法 → 无更新/逻辑删除入口。
- [x] **逻辑删除**：全库统一 `deleted` 整数字段软删（审计表除外）。
- [x] **唯一索引×逻辑删除**：`SysUserMapper.countByUsernameIncludingDeleted` 等绕过逻辑删除过滤避免唯一键冲突笼统 409（标识符不回收）。

### §4 传输与基础设施
- [x] **CORS**：`CorsConfig` 非 dev 下 `CORS_ALLOWED_ORIGINS` 为空/含 `*`/含空白 → `IllegalStateException` 启动即失败（fail-fast）；且 CorsFilter 顺序 `HIGHEST_PRECEDENCE` 早于鉴权过滤器，保证被短路响应也带 CORS 头。
- [x] **零下行硬控红线**：`HardControlInterceptor` 对 `HARD_CONTROL_PATHS` 写操作抛 `BusinessException(ResultCode.HARD_CONTROL_BLOCKED=503)`，绝不复现真实硬件动作。
- [x] **H2 Console**：`application-prod.yml`/`application-dm.yml` 已 `enabled:false`（dev 外关闭）。
- [风险] **HTTPS / actuator 暴露面**：属 prod 网络/反代配置（nginx TLS 终止、`/actuator/prometheus` 限内网），待 prod 部署确认。
- [阻塞] **容器/部署隔离**：`deploy/docker-compose.yml` 用 `env_file` 注入敏感项（不写进 `environment:`）；但 daemon DOWN，compose 未 `up --build` 实跑。

### §5 输入与依赖
- [x] **参数校验**：Controller 入口 `@Valid`；`AuditEvent.action` 必填、批量上限（见 `docs/architecture/audit-log.md`）；非法枚举→B3 `PARAM_INVALID(100)`。
- [x] **SQL 注入**：全量 MyBatis-Plus / 参数化；`src/main/resources` 下 **所有 mapper XML 无 `${}` 字符串拼接**（grep `${` 0 命中），无拼接入口。
- [阻塞] **依赖漏洞**：`pom.xml` 未集成 `owasp-dependency-check`；离线 Maven 无法拉 CVE 库。需联网环境或本地 NVD 镜像后执行 `mvn org.owasp:dependency-check-maven:check`。
- [风险] **CSP**：`frontend-scaffold/deploy/csp.conf` 的 nonce 为占位 `REPLACE_WITH_GATEWAY_NONCE`，默认关闭；启用前须网关注入真实 nonce 并验证链路（否则白屏）。已知非缺陷限制。

### §6 审计防伪造
- [x] **方案 A 已闭环**（清单原 `[x]`）：`fac_audit_log.actor` 由服务端按登录态写入，客户端不可伪造。残余风险（审计内容前端自断言）已登记 accepted-risk 待甲方签字。

## 2. 四段式风险/阻塞登记

### R1 · 默认 admin 种子（prod 必须处置）
- **现象**：`AuthService.ensureAdmin()` 在 dev 写入 `admin/admin@2026`。
- **影响**：若 prod 沿用，存在弱口令暴露面。
- **证据**：`AuthService.java` 种子逻辑；`password-security.md` 述 prod 须 `force-change-default-admin=true` 或删账号。
- **建议**：prod 启动前确认该种子被禁用/改密；纳入 release 窗口 §0 默认账号核查。

### R2 · 依赖漏洞未扫（SCA 缺口）
- **现象**：pom 无 owasp 插件，离线无法跑 CVE 库。
- **影响**：阶段 7 验收缺一项安全证据（尤其达梦驱动 `DmJdbcDriver18-8.1.3` 版本核对）。
- **证据**：`grep -i dependency-check pom.xml` → 0 命中。
- **建议**：联网环境加 `owasp-dependency-check-maven` 插件跑一次；或甲方提供 NVD 镜像地址。阻塞项，不阻塞其余交付。

### R3 · Docker daemon 未起（部署演练阻塞）
- **现象**：`docker info` → npipe 连接失败，daemon DOWN。
- **影响**：prod 联调、compose 实跑、PG 容器均无法执行。
- **证据**：本会话 `docker info` 探测输出。
- **建议**：启动 Docker Desktop（或 `com.docker.service`）后执行 `docker compose up --build` + PG 容器 prod profile 联调。属环境阻塞，非代码缺陷。

## 3. 结论
- **本机已静态坐实（通过）**：写端点授权 0 违规、JWT/签名密钥 fail-fast、BCrypt、CORS fail-fast、零下行硬控 503、审计不可变、逻辑删除、SQL 注入无拼接、ABAC 纯配置无硬编码、CSP 占位已知限制。
- **待实跑（风险）**：白名单/水平越权/系统域硬防护/WS 鉴权/ABAC 三态/默认账号处置/HTTPS/actuator——代码就绪，需 dev 或 prod 实例跑验证脚本。
- **阻塞**：依赖漏洞扫描（离线）、Docker 部署演练（daemon）、甲方问卷回收（已出草案）。
- 清单 `penetration-checklist.md` 本体 `[ ]` 条目保持为 release 窗口执行项；本自审覆盖其离线可证部分。
