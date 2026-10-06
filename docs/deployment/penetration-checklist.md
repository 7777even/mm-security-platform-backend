# 安全渗透复核清单（Penetration Checklist）

> 阶段 7「安全渗透复核」的可执行复核手册。覆盖本系统**领域特有**的硬控红线 + 通用 Web 安全。
> 配套真源：`docs/architecture/{auth-design,password-security,filter-chain,data-masking,audit-log}.md`。
> 复核结论记入 `engineering/retro/`（四段式 + 证据必附，模板见 `engineering/retro-qa-tracker.md`）。
>
> **本手册事实基线（2026-10-06）**：dev=H2 文件库（`data/mm_security_dev.mv.db`，重启保留数据）；三方言（H2/PostgreSQL/达梦 DM8）迁移**均已真机跑通**；ABAC 实时广播防区收紧代码+推荐默认映射已就绪（待产品确认替换配置）；写端点授权 148/148（门禁 `scripts/check-endpoint-authz.mjs` 0 违规）。

## 0. 复核范围与前置

- [ ] **输入齐备**：`customer-environment-questionnaire.md` 已回收，§3 安全/合规项（等保级别、CORS 域名、防重放、渗透窗口）已有甲方确认值。
- [ ] **目标环境**：prod / dm profile 已起独立实例（非 dev）；`JWT_SECRET`/`SIGNATURE_SECRET`/`CORS_ALLOWED_ORIGINS` 经环境变量注入、缺失即 fail-fast（见 `docs/deployment/README.md §6`）。
- [ ] **工具就绪**：Python(utf-8) 冒烟脚本（中文 body 禁用裸 `curl`，GBK 乱码）；`scripts/smoke-*.cjs`；可选 `owasp-zap` / `nmap`。
- [ ] **默认账号**：确认 `admin/admin@2026` 在生产已被改密或删除（`AuthService.ensureAdmin` 种子账号不应存活）。

## 1. 认证与授权

- [ ] **JWT 密钥强度**：`JWT_SECRET` ≥ 32 字节、非占位、非 dev 默认值；缺失/占位 → 启动即 `SecurityBeans.validateSecrets()` 抛异常（fail-fast）。
- [ ] **刷新令牌不进 body**：`POST /auth/refresh` 响应体无 refresh 字段；refresh 仅经 `HttpOnly`+`SameSite=Lax` Cookie(`name=rt`) 下发；`logout` 清 Cookie。
- [ ] **白名单口径**：`JwtFilter` 仅 `/auth/{login,refresh,logout}` 与 `/ws` 免 `Authorization` 头校验；`/auth/me`、`/auth/menus` **不在**白名单（须带 access 令牌，契约声明 401/403）。
  - 验证：`curl -i /api/v1/auth/me` 无令牌 → **401**（非 200）。
- [ ] **鉴权失败语义**：缺/过期令牌 → 401；角色不符 → 403；均包 `Result<T>`、不冒泡 500；响应带 CORS 头。
- [ ] **写端点授权覆盖**：`node scripts/check-endpoint-authz.mjs` 输出 **0 违规**（148 写端点全约束，11 豁免项均有登记理由且名单未腐烂）。
- [ ] **水平越权**：`AuthorizationService.assertSelfOrAdmin` 在资源归属接口生效（如 `/auth/password`、`/auth/profile`、现场回传 reporter 须本人或 ADMIN）。
  - 验证：普通用户 A 持令牌调 `PUT /api/v1/auth/profile` 改用户 B 资料 → **403**。
- [ ] **系统管理域硬防护**：服务端强制（不依赖前端禁用按钮）：禁删/禁停用/禁改自己角色（403）；保护最后一个启用 ADMIN（409）；内置角色/内置字典禁删禁停（403）；角色被引用/字典有项/菜单有子节点或已授权时禁删（409）。
  - 验证：普通 ADMIN 调 `DELETE /api/v1/system/users/<self>` → **403**；停用最后一个 ADMIN → **409**。
- [ ] **防重放**：生产 `signature.enabled=true`，`X-Timestamp`/`X-Nonce`/`X-Signature` 校验、300s 容忍窗口；重放同一请求（复用 nonce）→ **拒绝**。
- [ ] **强制首登改密**：生产 `app.password.force-change-default-admin=true`；`PasswordLifecycleInterceptor` 对变更类请求校验 `must_change_pwd`，命中 → 403（豁免 `/auth/**` 与 `/uplink/audit`）。dev 关闭（`false`）属联调契约，生产必须 true。

## 2. 实时广播 WS 鉴权与防区过滤（领域特有）

> 零下行控制红线：WS 仅下 `<domain>.changed` 刷新通知，绝不携带下行控制指令。

- [ ] **WS 握手鉴权**：`/ws/alarm` 必须带有效 `?token=`（access 令牌）；无效/过期/`TokenVersion` 不符 → 握手 **401 拒绝升级**。
  - 验证：`websocat ws://host:8787/ws/alarm`（无 token）→ 升级失败；带 `?token=<伪造>` → 401。
- [ ] **ABAC 防区过滤三态**：`RealtimeBroadcastService.broadcast` 按 `zone_codes` 过滤——会话 ALL(null) 推全部；事件未映射(zones=null) fail-open 推全部已认证；二者皆非空仅交集命中推（最小权限）。
  - 验证：用防区 A 账号订阅，仅应收到防区 A 域变更；未配置映射时全推（与现状一致，属预期 fail-open）。
- [ ] **location→防区 映射来源**：收紧规则 100% 来自配置 `abac.zone-mapping`（代码零硬编码）；产品确认规则后只改 yml 不动 Java。复核时确认 prod 配置已填真实语义（当前 dev 为推荐默认，待替换）。
- [ ] **删除类写方法防区**：返回 void/`DeleteResult` 的删除域自然 fail-open（已知限制，非缺陷）；`system.user` 域因 `zoneCodes` 多值未接线（避免错误收紧）。

## 3. 数据安全与隐私

- [ ] **口令存储**：`sys_user.password_hash` 为 BCrypt 哈希，无明文、无可逆加密；日志/响应不含口令或哈希（`RbacBootstrapService` 已修正明文打印）。
- [ ] **数据脱敏**：PII（真实姓名、设备编码、身份证）在日志 MDC 与响应出口脱敏（见 `docs/architecture/data-masking.md`）；复核时确认已实施而非仅设计。
- [ ] **审计不可变**：`fac_audit_log` 无逻辑删除、无更新入口；写入前 `detail` 已脱敏。
- [ ] **逻辑删除**：全库统一 `deleted` 整数字段软删；无业务数据被物理硬删（审计表除外）。
- [ ] **唯一索引×逻辑删除语义**：`sys_user.username`/`sys_role.role_code`/`sys_dict_type.dict_code` 删除走 `countXxxIncludingDeleted`（绕过逻辑删除过滤）避免唯一键冲突笼统 409；标识符不回收。

## 4. 传输与基础设施

- [ ] **HTTPS / 传输加密**：生产 `COOKIE_SECURE=true`（refresh Cookie 仅 HTTPS 生效）；全链路 TLS（nginx 反代终止 TLS，见 `docs/deployment/README.md §4.3`）。
- [ ] **CORS**：非 dev profile `CORS_ALLOWED_ORIGINS` 显式白名单、不含 `*`；含 `*`/空白 → 启动即 `IllegalStateException`。
  - 验证：非白名单 Origin 跨域请求 → 无 `Access-Control-Allow-Origin` 回显。
- [ ] **零下行硬控红线**：`HardControlInterceptor` 对 `HARD_CONTROL_PATHS` 写操作返回 **503 `HARD_CONTROL_BLOCKED`**（安全管控领域红线，专项验证；见 `docs/architecture/filter-chain.md`）。
  - 验证：模拟下行控制请求（如远程关阀/断电类路径）→ 503 阻断，不复现真实硬件动作。
- [ ] **actuator 暴露面**：`/actuator/health`、`/actuator/prometheus` 仅限内网/监控网段，不暴露公网（`prometheus` 免鉴权，见 `README.md §7`）。
- [ ] **H2 Console**：`dev` 外关闭（`application-dm.yml`/`prod` 已 `enabled:false`）。
- [ ] **容器/部署隔离**：compose/Dockerfile 实跑验证（本仓 compose 结构已过语法校验、未实跑）；`deploy/docker-compose.yml` 不将敏感项写进 `environment:`（用 `env_file` 注入，避免空串覆盖 dev 密钥致启动失败）。

## 5. 输入与依赖

- [ ] **参数校验**：`@Valid` 在 Controller 入口生效；审计 `AuditEvent.action` 必填校验、批量大小有上限；非法枚举（如 `status` 不在白名单）→ B3 `PARAM_INVALID`(100)。
- [ ] **SQL 注入**：全部走 MyBatis-Plus / 参数化，无字符串拼接 SQL（`scripts/sql_stmt_scan.py` 仅校验迁移终结符，应用层依赖框架参数绑定）。
- [ ] **依赖漏洞**：`mvn dependency:check`（或 SCA）无高危；含达梦驱动 `DmJdbcDriver18-8.1.3` 版本核对（仅在 dm profile）。
- [ ] **CSP**：`deploy/csp.conf` 的 nonce 须由入口网关/OpenResty 注入真实值后启用（占位 `REPLACE_WITH_GATEWAY_NONCE` 会导致内联脚本被拦白屏）；启用前须先验证 nonce 注入链路。

## 6. 审计防伪造（已处置 · 方案 A）

- [x] **`POST /api/v1/audit/log` 提交人溯源（方案 A）**：已实现——`fac_audit_log` 新增 `actor` 列（三方言 V108 迁移），`UplinkService.reportAudit` 由服务端按当前登录态（`UserContext.username()`）写入提交人，客户端不可伪造；保留 `@RequireAuth` 登录态口径、不加 `role=ADMIN`（不挡死普通用户审计上报）。`GET /api/v1/audit/log` 透出 `actor`，前端审计页增"操作人"列。
  - 决策文档：`docs/deployment/audit-anti-forgery-decisions.md`（A/B/C 对比，用户拍板 A）。
  - OpenSpec Change：`2026-10-06-uplink-audit-anchor-submitter`（已合入 `openspec/specs/uplink-audit/spec.md`）。
  - **结论：通过**（提交人溯源已落地；self-report 内容语义保留，待甲方最终确认是否需进一步约束——见决策文档 §7）。
  - 残余风险：审计内容仍由前端自我断言（服务端无法独立验证"用户真点了哪个按钮"），但每行可归因于真实登录，满足等保"审计记录可定位到主体"核心要求；该残余风险已登记为 accepted-risk 待甲方签字（决策文档 §3 方案 C 路径）。

## 7. 复核产出与证据规范

- 每个 `[ ]` 项标注 **通过 / 风险 / 阻塞**，附证据（截图 / Python curl 输出 / 日志片段 / `mvn`/`node` 脚本输出）。
- 风险项登记到 `engineering/retro/`（四段式：现象 / 影响 / 证据 / 建议），并回链对应 OpenSpec Change。
- 阻塞项升级为 L4 门禁（须人工决策后实施）。
- 复核报告同步甲方，等保条款对齐 `customer-environment-questionnaire.md §3`。
- 复核完成后将本清单状态回填 `docs/architecture/roadmap.md` 阶段 7「安全渗透复核」项，置 ✅。
