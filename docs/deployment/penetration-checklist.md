# 安全渗透复核清单（Penetration Checklist）

> 阶段 7「安全渗透复核」的执行清单。覆盖本系统**领域特有**的硬控红线 + 通用 Web 安全。与 `auth-design.md`、`password-security.md`、`filter-chain.md` 配合。复核结论记入 `engineering/retro/`（四段式 + 证据必附）。

## 1. 认证与授权

- [ ] **默认账号处置**：生产环境 `admin/admin@2026` 已改密或删除（`AuthService.ensureAdmin` 种子账号不应存活于生产）。
- [ ] **JWT 密钥**：`JWT_SECRET` ≥ 32 字节、非占位、经环境变量注入；缺失即启动失败（验证 `SecurityBeans.validateSecrets` fail-fast）。
- [ ] **刷新令牌不进 body**：`POST /auth/refresh` 响应无 refresh 字段；refresh 仅经 `HttpOnly` + `SameSite=Lax` Cookie（`name=rt`）下发；`logout` 清 Cookie。
- [ ] **白名单口径**：`/auth/me`、`/auth/menus` **不在** `JwtFilter.WHITELIST`（必须带 access 令牌）；`/auth/login|refresh|logout` 在白名单。
- [ ] **鉴权失败语义**：缺/过期令牌 → 401（非 200+code）；角色不符 → 403；均包 `Result<T>`、不冒泡 500；响应带 CORS 头。
- [ ] **水平越权**：`AuthorizationService.assertSelfOrAdmin` 在资源归属接口生效（如现场回传 reporter 必须是本人或 ADMIN）。
- [ ] **防重放**：生产 `signature.enabled=true`，`X-Timestamp`/`X-Nonce`/`X-Signature` 校验、300s 容忍窗口。

## 2. 数据安全与隐私

- [ ] **口令存储**：`sys_user.password_hash` 为 BCrypt 哈希，无明文、无可逆加密；日志/响应不含口令或哈希。
- [ ] **数据脱敏**：PII（真实姓名、设备编码、身份证）在日志 MDC 与响应出口脱敏（见 `data-masking.md`；当前为设计待落地，复核时需确认是否已实施）。
- [ ] **审计不可变**：`fac_audit_log` 无逻辑删除、无更新入口；写入前 `detail` 已脱敏。
- [ ] **逻辑删除**：全库统一手动 `deleted` 整数字段，无数据被物理硬删（含审计表）。

## 3. 传输与基础设施

- [ ] **HTTPS / 传输加密**：生产 `COOKIE_SECURE=true`（刷新 Cookie 仅 HTTPS 生效）；全链路 TLS。
- [ ] **CORS**：非 dev profile `CORS_ALLOWED_ORIGINS` 显式白名单、不含 `*`；含 `*`/空白启动即 `IllegalStateException`。
- [ ] **零下行控制**：`HardControlInterceptor` 对 `HARD_CONTROL_PATHS` 的写操作返回 503 `HARD_CONTROL_BLOCKED`（安全管控领域红线，须专项验证）。
- [ ] ** actuator 暴露面**：`/actuator/health`、`/actuator/prometheus` 仅限内网/监控网段，不暴露公网。
- [ ] **H2 Console**：`dev` 外关闭（`application-dm.yml` / `prod` 已 `enabled:false`）。

## 4. 输入与依赖

- [ ] **参数校验**：`@Valid` 在 Controller 入口生效；审计 `AuditEvent.action` 必填校验、批量大小有上限。
- [ ] **SQL 注入**：全部走 MyBatis-Plus / 参数化，无字符串拼接 SQL。
- [ ] **依赖漏洞**：`mvn dependency:check` / SCA 扫描无高危（含达梦驱动版本核对）。
- [ ] **CSP**：`deploy/csp.conf` nonce 须由入口网关/OpenResty 注入真实值后再启用（占位 nonce 会导致白屏）。

## 5. 复核产出

- 每个 `[ ]` 项标注 **通过 / 风险 / 阻塞**，附证据（截图 / curl 输出 / 日志片段）。
- 风险项登记到 `engineering/retro/` 并回链对应 OpenSpec Change；阻塞项升级为 L4 门禁。
- 复核报告同步给甲方（参照 `customer-environment-questionnaire.md §3` 的等保要求）。
