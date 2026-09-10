# 口令与密钥安全（Password & Secret Security）

> 凭据存储与密钥管理的设计真源。任何密码编码方式、密钥来源、默认账号策略的变更属 **L4**，须 proposal + 评审。本文与 `auth-design.md`、`AGENTS.md §6.3` 互为镜像。

## 1. 决策（Decisions）

- **单向哈希，绝不明文**：用户口令经 `BCryptPasswordEncoder`（Spring Security，默认 strength=10）哈希后存 `sys_user.password_hash`；登录时用 `encoder.matches(raw, hash)` 比对，**系统任何位置都不保留明文口令**。
- **密钥只进环境变量**：`jwt.secret` / `signature.secret` 仅经 `JWT_SECRET` / `SIGNATURE_SECRET` 注入；base `application.yml` 不写默认值（防误提交弱密钥）。
- **启动期密钥强度校验**：`SecurityBeans.validateSecrets()` 校验 JWT 密钥 ≥ 32 字节且非已知占位集合、签名密钥 ≥ 16 字节且非已知占位，违反即 `IllegalStateException` 拒绝启动。
- **默认管理员一次性种子（2026-09-10 迁至 `RbacBootstrapService`）**：仅在 `sys_user` 表为空时写入 `admin / admin@2026`；非空即跳过，不在运行时反复重置。是否置 `must_change_pwd=1`（强制首登改密）由 `app.password.force-change-default-admin` 控制：**生产默认 true**（`admin@2026` 属已知弱口令），**dev 为 false**（H2 内存库每次重启重建，强制改密会反复阻断联调）。
- **口令生命周期（V32/V33 起）**：`POST /auth/password` 本人改密（**必须校验旧口令** + 通过 `PasswordPolicy` 复杂度策略，成功后清 `must_change_pwd`、更新 `pwd_updated_at`）；`POST /system/users/{id}/password/reset` 管理员重置（服务端随机 12 位临时口令，**绝不用固定值**，置 `must_change_pwd=1`，一次性返回）。
- **口令复杂度策略（`PasswordPolicy`，配置化）**：`app.password.{enabled,min-length,require-categories}`；默认长度 ≥ 8、须覆盖大写/小写/数字/符号中 **3 类**、不得包含用户名、不得与旧口令相同。违规抛 `code=100`（HTTP 200），由前端按 message 提示。
- **强制首登改密的服务端兜底**：`PasswordLifecycleInterceptor` 对变更类请求（非 GET/HEAD/OPTIONS）拒绝未改密账号（403），豁免 `/api/v1/auth/**` 与 `/api/v1/uplink/audit`；前端引导为辅助，**不构成唯一防线**。
- **登录失败不泄露账号是否存在**：用户名不存在与口令错误统一返回 `401 用户名或密码错误`；禁用账号返回 `401 账号已被禁用`。

## 2. 现状（Current State）

| 项 | 位置 | 状态 |
| -- | ---- | ---- |
| BCrypt Bean | `config/SecurityBeans.bCryptPasswordEncoder()` | ✅ |
| 口令字段 | `entity/SysUser.passwordHash` | ✅ |
| 登录比对 | `service/AuthService.login()`（`encoder.matches`） | ✅ |
| 默认账号种子 | `service/RbacBootstrapService.ensureAdminUser()`（2026-09-10 自 `AuthService` 拆出） | ✅ |
| 口令复杂度策略 | `service/PasswordPolicy`（配置化 `app.password.*`） | ✅ |
| 本人改密 | `controller/AuthController.changePassword` + `service/AccountService` | ✅ |
| 管理员重置口令 | `service/SystemUserService.resetPassword`（随机临时口令 + 强制改密） | ✅ |
| 强制改密服务端兜底 | `security/PasswordLifecycleInterceptor` + `common/cache/PasswordStateCache` | ✅ |
| 密钥校验 | `config/SecurityBeans.validateSecrets()`（@PostConstruct） | ✅ |
| 已知弱密钥兜底拦截 | `SecurityBeans.KNOWN_WEAK_JWT_SECRETS` / `KNOWN_WEAK_SIGN_SECRETS` | ✅ |

## 3. 约束（Constraints）

- `password_hash` 字段**永不出响应体**（DTO 映射排除；`/auth/me` 返回 `MeResult`，只含 `username/realName/role/roles/perms/mustChangePwd`）。
- 口令**绝不出现在审计 detail 中**：`SystemAuditHelper` 只记事实（`username` 等），管理员重置口令的临时值**只在响应里一次返回**，不落审计、不落日志。
- **用户名 / 角色标识 / 字典标识不回收**：三者均有唯一索引而删除是逻辑删除，唯一性判定须**含已删行**（`Sys*Mapper.countXxxIncludingDeleted`）。回收标识会让新旧记录在审计上无法区分，故有意拒绝。
- 禁止在日志、异常栈、`Result` 详情中输出明文口令或口令哈希。
- 密钥轮换：改 `JWT_SECRET` / `SIGNATURE_SECRET` 后**所有已签发令牌立即失效**（无状态、无黑名单），属有意为之；需在变更窗口内通知前端重新登录。
- 达梦 / PG / H2 三方言下 `password_hash` 列类型须一致（varchar 足够，BCrypt 哈希固定 60 字符）。

## 4. 反模式（Anti-patterns）

- ❌ 明文存储口令或用可逆加密（AES/Base64 伪装）—— 数据泄露即全员裸奔。
- ❌ 在 base `application.yml` 写死默认密钥 —— 一旦提交即弱密钥常态化。
- ❌ 登录失败区分「用户不存在」与「口令错误」—— 给枚举攻击留口。
- ❌ 把 `admin@2026` 当作生产凭证 —— 上线前必须改密或删除默认账号（属 P0-3 渗透清单核对项）。
- ❌ 自造哈希（MD5/SHA 无盐）—— BCrypt 自带盐与自适应成本，禁止重复造轮子。
