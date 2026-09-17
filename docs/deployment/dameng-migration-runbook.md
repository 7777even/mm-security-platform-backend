# 达梦 DM8 迁移手册（Dameng Migration Runbook）

> 信创生产目标库的迁移实战步骤。当前 `application-dm.yml` + `db/migration/dameng`（**V1–V57 已与 h2/pg 对齐补齐；语句终结符于 2026-09-17 补齐至真正完整，并通过升级后的三方言静态一致性校验，0 结构性错误**）已就绪，且**已于 2026-09-17 在真实 DM8 实例上实跑通过**（V1–V57 全部 57 个文件逐条执行，`[-NNNN]` 错误码 **0**，建表 140）。本手册与 `docs/deployment/README.md §5` 互为补充。

## 1. 现状与关键缺口（必须先读）

| 资产 | 位置 | 状态 |
| ---- | ---- | ---- |
| DM8 profile | `src/main/resources/application-dm.yml` | ✅ 配置就绪（JWT/签名/CORS/日志基线齐） |
| DM 迁移脚本 | `src/main/resources/db/migration/dameng/` | ✅ **V1–V57 已与 h2/pg 对齐补齐**（早期仅 V1/V2；2026-09-14 起补齐 V3–V57；2026-09-16 首轮补终结符**不完整**，2026-09-17 补齐 23 文件 1303 处并升级校验判据）。**2026-09-17 已在真实 DM8 实例实跑通过**（错误码 0、建表 140，详见 §5） |
| H2 迁移脚本（参照基线） | `src/main/resources/db/migration/h2/` | ✅ 已到 **V57**（与 dameng/postgresql 三方言一致） |
| PG 迁移脚本 | `src/main/resources/db/migration/postgresql/` | ✅ 已到 **V57**（与 h2/dameng 三方言一致）。**2026-09-17 已用 Docker + Flyway 10 真实实跑通过**（曾抓到并修 `V40` 布尔列 `DEFAULT 1` 的 PG 非法写法） |
| 三方言静态一致性校验 | `scripts/check-dialect-migration-consistency.py`（终结符审计复用 `scripts/sql_stmt_scan.py`） | ✅ 已接入后端 CI（`dialect-consistency` job）。**2026-09-17 起终结符判据改为括号深度状态机、并升级为阻断性 error**（旧判据「CREATE TABLE 数 > 分号数」对达梦种子检出率为 0 且只 warn 不阻断，曾放行残缺补丁） |
| DM JDBC 驱动 | `pom.xml` `<profiles><dm>` 已声明依赖 | ⚠️ 驱动 jar 不在中央仓库，需 `mvn install:install-file` 本地安装（本机验证时可直接用达梦自带的 `drivers/jdbc/DmJdbcDriver11.jar`） |
| DM 实跑执行器 | `scripts/verify-dameng-migration.cmd` + `dameng-drop-all.sql` | ✅ 2026-09-17 新增，**已用于完成 V1–V57 全量实跑**（详见 §5） |

> ⚠️ **历史风险已消除**：DM 迁移早期曾落后 H2 多个版本（仅 V1/V2），现已补齐至 **V57** 并与 h2/pg 保持三方言版本/表/列名一致；后续新增 DM 方言版本仍须遵守「V-file 禁改/删、新增只加 V*+」铁律。此前「未在真实 DM8 实例实跑」这一唯一未闭合风险，**已于 2026-09-17 消除（见 §5）**。

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
- ⚠️ **逐条 INSERT 必须「每条」以 `;` 结尾**：达梦种子被拆成一行一条后，2026-09-16 的首轮回填只补了 `CREATE TABLE` 的 `);`，**漏掉全部 INSERT/UPDATE/DELETE**（如 `V19` 补后仅 22 个 `;`，实际需 497）→ Flyway 无法切分、DM8 实跑必失败；H2 侧无此问题故本地不暴露。新增/修改达梦迁移后务必跑 CI 的 `dialect-consistency` job（现已能检出），或用 `python scripts/fix-dameng-terminators.py --migration-dir src/main/resources/db/migration --dialect dameng --dry-run` 自查。
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

## 5. 迁移校验（2026-09-17 实跑方式与结论）

### 5.1 方式一（推荐）：Flyway 集成 —— 2026-09-17 已打通

⚠️ **Flyway 社区版与商业版均不内置达梦 database 模块**（直接启动报 `Unsupported Database`）。解法是在 `pom.xml` 的 **`dm` profile 内**引入第三方扩展并抬升 Flyway 版本（仅在 `-Pdm` 时生效，dev/PG 零影响）：

```xml
<profile>
  <id>dm</id>
  <properties>
    <flyway.version>10.10.0</flyway.version>   <!-- 必须，原因见下 -->
  </properties>
  <dependencies>
    <dependency>
      <groupId>com.github.mengweijin</groupId>
      <artifactId>db-migration-dameng-flyway</artifactId>
      <version>4.1.2</version>                 <!-- Apache-2.0 -->
    </dependency>
  </dependencies>
</profile>
```

**三个必须踩过的坑**（均已在真机验证）：

1. **Flyway 版本必须 ≥ 10**：该扩展调用了 `JdbcConnectionFactory.isSupportsBatch()`，此方法 **9.22.3 不存在 / 10.10.0 存在**（`javap` 对照确认）。其 README 标称「2.2.1 兼容 Spring Boot 3.2.x + Flyway 9.22.3」与实际不符，实测抛 `NoSuchMethodError` ⇒ 采用官方矩阵中的 **4.1.2 + Flyway 10.10.0** 组合。
2. **`baseline-version: 0` 不可省**：达梦实例**恒自带系统表**（`##HISTOGRAMS_TABLE` 等），继承 `baseline-on-migrate=true` 后 Flyway 会判定「schema 非空」并按**默认 baselineVersion=1 打基线 ⇒ `V1__init_schema.sql` 被整份跳过**，随后 V2 的种子写 `sys_menu` 即报 `-2104 无效的表名`。`application-dm.yml` 已显式设 `baseline-version: 0`。
3. **JDBC URL 不可带路径段**：达梦把 URL 路径当**模式名(schema)** 解析 —— `jdbc:dm://host:5236/mm_security` 直接报「无效的模式名[mm_security]」（达梦「用户 = 模式」，schema 由 `DB_USERNAME` 决定）。

### 5.2 方式二（无 Flyway 时的兜底）：用 disql 逐文件执行

若目标环境不具备引入第三方 Flyway 扩展的条件，可改用达梦自带 `disql` 逐文件执行 `db/migration/dameng/` 下的 V1–V57，以验证「SQL 本身能否被 DM8 接受」。执行器：`scripts/verify-dameng-migration.cmd`。

```cmd
rem 用【管理员身份】的 cmd（bin 下工具需提权，普通 cmd 报「拒绝访问。」）
rem 单行粘贴，避免换行丢失：
cd /d <repo>\backend-scaffold && set "CLEAN=0" && scripts\verify-dameng-migration.cmd
```

- 脚本生成 master 脚本（`SET CHAR_CODE UTF8` / `SET LOCAL_CODE UTF8` / `SET DEFINE OFF`，随后逐文件 `start "<绝对路径>"`，末尾 `exit`）整体灌给 `disql`；用 `start` 而非直灌是**为了中文编码正确**（见下）。
- 每个文件前插 `select 'MARKER_Vn 文件名' from dual;`，日志可按文件切分定位。
- `CLEAN=1`（默认）先跑 `scripts/dameng-drop-all.sql`（反推 140 表、按版本倒序 `DROP TABLE x CASCADE;`）；**空实例上务必 `set CLEAN=0`**，否则刷 140 条「无效的表或视图名」噪音。
- 日志落在仓库父目录 `dameng-migrate.log`（非 git 仓，不会被误提交）。

> ⚠️ **disql 编码铁律**：`SET CHAR_CODE` 只对 `start "脚本"` 生效，对 `disql < file`（stdin 直灌）**不生效** ⇒ 直灌的脚本必须**零非 ASCII 字节（连注释都不行）**，否则整个脚本会静默零输出、极难察觉；需要带中文的验证脚本，改用 `LENGTH()`/`LENGTHB()` 断言以纯 ASCII 表达。

### 5.3 实测结论（2026-09-17，本机 DM8 开发版）

- **`[-NNNN]` 错误码 0 个**（历次收敛 2497 → 186 → 7 → **0**）；57 个文件全部执行（MARKER 完整覆盖 V1–V57）。
- `CREATE TABLE` 回显 **140** 次，与三方言静态对拍的「表总数 140」一致（库里 `user_tables` 报 141，系多出达梦内置的 `##HISTOGRAMS_TABLE`，非业务表）。
- **中文正确落库**：`fac_perimeter_alarm.false_alarm` 存「未核实」，`LENGTH()`=3 字符 / `LENGTHB()`=9 字节，符 UTF-8。
- 实例参数：`charset=1`(UTF-8)、`string case sensitive=0`、`page size=16384`；**`LENGTH_IN_CHAR` 自 2024 Q2 已废弃**，无法设置 → 字符语义靠字段级 `VARCHAR2(n CHAR)`。
- 本轮修掉的 DM8 专有缺陷：① `GENERATED BY DEFAULT AS IDENTITY` 被拒 → `IDENTITY(1,1)`；② identity 列种子显式赋 id 须 `SET IDENTITY_INSERT <表> ON/OFF`；③ `domain` 属 DM8 保留字 → 列改 `domain_code` + 实体 `@TableField`；④ 字节计长 → 42 文件 / 778 处改 `VARCHAR2(n CHAR)`。
- **Flyway 集成与应用启动均已验证通过**：`Successfully applied 57 migrations to schema "SYSDBA", now at version v57`（迁移耗时 ~4s）；随后 `Started Application in 24.19s`，11 个关键端点全部 **200**、`/auth/me` 返回 `role=ADMIN / perms=31`、中文读出无乱码（`location=装置C`、`typeLabel=火灾报警`、`name=乙烯（Ethylene）`、`category=应急广播`）。迁移后库内 `user_tables = 142` = **140 业务表** + 达梦内置 `##HISTOGRAMS_TABLE` + `flyway_schema_history`。

### 5.4 静态复核（每次改迁移后，CI 已自动跑）

`python scripts/check-dialect-migration-consistency.py --migration-dir src/main/resources/db/migration --dialects h2,dameng,postgresql`；
只查终结符可用 `python scripts/sql_stmt_scan.py --migration-dir src/main/resources/db/migration --dialects h2,dameng,postgresql`。

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

> 本手册随达梦实测推进持续更新；**2026-09-17 首次实跑已完成，方式与结论见 §5**。后续若接入第三方 Flyway 达梦 database 模块，把「Flyway 兼容模式配置」回填 §5.1。
