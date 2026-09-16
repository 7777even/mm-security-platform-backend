# 达梦 DM8 迁移手册（Dameng Migration Runbook）

> 信创生产目标库的迁移实战步骤。当前 `application-dm.yml` + `db/migration/dameng`（**V1–V57 已与 h2/pg 对齐补齐、语句终结符已补、并通过三方言静态一致性校验，0 结构性错误**）已就绪，但**未在真实 DM8 实例实跑**。本手册是具备达梦环境后的操作清单，与 `docs/deployment/README.md §5` 互为补充。

## 1. 现状与关键缺口（必须先读）

| 资产 | 位置 | 状态 |
| ---- | ---- | ---- |
| DM8 profile | `src/main/resources/application-dm.yml` | ✅ 配置就绪（JWT/签名/CORS/日志基线齐） |
| DM 迁移脚本 | `src/main/resources/db/migration/dameng/` | ✅ **V1–V57 已与 h2/pg 对齐补齐**（早期仅 V1/V2；2026-09-14 起补齐 V3–V57，2026-09-16 补语句终结符 + 静态校验通过） |
| H2 迁移脚本（参照基线） | `src/main/resources/db/migration/h2/` | ✅ 已到 **V57**（与 dameng/postgresql 三方言一致） |
| PG 迁移脚本 | `src/main/resources/db/migration/postgresql/` | ✅ 已到 **V57**（与 h2/dameng 三方言一致） |
| 三方言静态一致性校验 | `scripts/check-dialect-migration-consistency.py` | ✅ 已接入后端 CI（`dialect-consistency` job），EXIT=0：版本/表/列名三方言对齐、终结符齐 |
| DM JDBC 驱动 | `pom.xml` `<profiles><dm>` 已声明依赖 | ⚠️ 驱动 jar 不在中央仓库，需 `mvn install:install-file` 本地安装 |

> ⚠️ **历史风险已消除（版本 parity 已完成）**：DM 迁移早期曾落后 H2 多个版本（仅 V1/V2），现已补齐至 **V57** 并与 h2/pg 保持三方言版本/表/列名一致。后续新增 DM 方言版本仍须遵守「V-file 禁改/删、新增只加 V*+」铁律。剩余唯一未闭合风险是**未在真实 DM8 实例实跑**（本机无实例、无 docker），详见 §5。

## 2. 前置条件（到环境上的操作）

1. **安装驱动**（授权限制，不随镜像分发）：
   ```bash
   mvn install:install-file -Dfile=DmJdbcDriver18.jar \
       -DgroupId=com.dameng -DartifactId=DmJdbcDriver18 -Dversion=8.1.3 -Dpackaging=jar
   ```
2. 确认 DM8 实例可达：`____:5236` 监听、账号 `SYSDBA` 或专用低权账号、库 `mm_security` 已建（或空库交 Flyway 建表）。
3. 准备 `.env`：从 `deploy/.env.example` 复制，设 `SPRING_PROFILES_ACTIVE=dm`，填 `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD/JWT_SECRET/SIGNATURE_SECRET/CORS_ALLOWED_ORIGINS/COOKIE_SECURE=true`。

## 3. 迁移脚本补齐（参考：手工补齐已完成）

- 以下方言改写规则在 2026-09-14~09-16 的 DM 补齐工作中已落实，保留作后续新增 DM 版本的参考：
  - `AUTO_INCREMENT` → 序列 + 触发器 或 `IDENTITY(1,1)`；
  - `BOOLEAN` → `NUMBER(1)`；`TEXT` → `CLOB`；`DATETIME` → `TIMESTAMP`；
  - 关键字冲突（如 `COMMENT`、`USER`、`LEVEL`）加双引号转义；
  - 自增主键 + MyBatis-Plus `IdType.AUTO` 在 DM 下需用 `IDENTITY`。
- 达梦因 Oracle 兼容语法不支持多行 `VALUES`，种子数据需拆为逐条 `INSERT`，且常量 `SELECT` 补 `FROM dual`（已由一次性脚本从 h2 版本派生）。
- 种子数据 `V2__seed_data.sql` 中的 `sys_menu`（5 个 fm-* 顶部菜单 + `allowed_roles`）必须与 `AuthService.menus()` 口径一致（见 `auth-design.md`）。

## 4. 构建与启动

```bash
# 含达梦驱动的镜像（或本地 jar）
docker build --build-arg BUILD_PROFILE=dm -t mm-security-backend:dm .
docker compose --profile dm up --build
# 或裸机
mvn -s ci-settings.xml package -DskipTests -Pdm
java -jar target/mm-security-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=dm
```

> 数据源变量是**自定义名** `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD`，非标准 `SPRING_DATASOURCE_*`。

## 5. Flyway 校验（首启）

- 观察启动日志：`Flyway` 应用 `dameng/V1..V57` 成功，`flyway_schema_history` 有 57 行。
- ⚠️ Flyway 社区版**无官方达梦 database 模块**：若报 `Unsupported Database`，需引入达梦兼容的 Flyway database 插件，或把 Flyway 的数据库探测配为兼容模式（Oracle 兼容）后重试。**此步必须在真实 DM 实例复核**，H2/PG 无法替代验证。
- 实跑前可先本地用 `python scripts/check-dialect-migration-consistency.py` 复核三方言结构与终结符（CI 已自动跑）。

## 6. 验收冒烟（对齐 README §7）

```bash
curl -s http://localhost:8787/actuator/health        # {"status":"UP"}
# 登录拿 accessToken（refresh 经 Set-Cookie，不进 body）
# GET /api/v1/auth/menus  -> 5 个 fm-* 顶部菜单
# GET /api/v1/devices / dashboard/overview -> 200
# ws://localhost:8787/ws/alarm -> {topic:'alarm.push', payload:{...}}
```
中文冒烟用 Python(utf-8)，勿裸 `curl` 带中文 body（Windows GBK 乱码）。

## 7. 回滚

- 应用回退：镜像/制品回退到上一版本。
- **库结构回退谨慎**：Flyway `clean` 会清空整库，**生产禁用**；如需回退版本，用新增一个 `V*+__rollback_*.sql` 的反向 DDL，而非 `clean`。
- 首次迁移建议先在**影子库**完整跑一遍再切生产。

> 本手册随达梦实测推进持续更新；首次实跑后把「实测通过的 DM 脚本」与「Flyway 兼容模式配置」回填本节。
