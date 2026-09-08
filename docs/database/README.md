# 数据库规约（Database Conventions）

> 与 `AGENTS.md §6`、架构 README §5 互补。DDL 变更一律走 Flyway 增量，禁止手改共享环境库。

## 1. 多 profile 数据源

| profile | 数据源 | 状态 | 说明 |
| ------- | ------ | ---- | ---- |
| `dev` | H2 内存库 `jdbc:h2:mem:mm_security` | ✅ 唯一可实跑 | `application-dev.yml` 覆盖；Flyway 走 `db/migration/h2`（V1 快照 + V2 种子） |
| `dm` | 达梦 DM8 | ⏸️ 暂缓 | 本机无实例/驱动/Docker；`application-dm.yml` + `db/migration/dameng` 保留作迁移资产 |
| `prod` | PostgreSQL | ⏸️ 回退 | 本机未装；`db/migration/postgresql` 存在 |

代码层 DB 无关（MyBatis-Plus 方言探测 + 不写方言函数），切换 profile 不改动 Java。

## 2. Flyway 双轨策略

- **V1 全量快照**：空库直达最新结构（含种子初始数据），用于本地 H2 一键起。
- **增量 V 文件**：命名 `V<yyyyMMddHHmmss>__<lower_snake_case>.sql`（如 `V20260908120000__add_fac_field_report.sql`）。
- **铁律**：已进入共享环境（含 CI / 演示库）的 **V 文件禁止修改 / 重命名 / 删除**。新增结构只加新 V，不回溯改旧 V。
- `spring.sql.init.enabled=false`，Flyway 独占建表职责；`baseline-on-migrate=true`。

## 3. 现有增量一览

| V | 内容 |
| - | ---- |
| V1 | 全量快照（所有业务表 + 系统表 + 种子） |
| V2 | 种子数据（默认账号 `admin/admin@2026` 由 `AuthService.ensureAdmin()` 注入，不进脚本） |
| V6 | `fac_field_report` 建表 + 2 条种子（现场回传真落库） |
| V7 | `sys_menu` `ALTER TABLE ADD COLUMN allowed_roles` + 重种 5 个 fm-* 顶部菜单（**不重建表**） |
| V8 | `sys_emergency_strength` / `sys_emergency_phone` / `sys_knowledge_item` / `sys_duty_member` 4 张参考表（应急力量/通讯录/知识库/值班由硬编码迁 DB，沿用原数值） |

## 4. 建表规约

- **主键**：业务表用业务自然键或雪花/UUID；`FacFieldReport.id` 用客户端 UUID（`IdType.INPUT`）以支持离线重发幂等。
- **逻辑删除**：全表统一 `deleted` 字段（`0` 未删 / `1` 已删），MyBatis-Plus 全局逻辑删除生效；禁止物理删除业务行。
- **审计字段**：新表须带 `created_at`（Unix 毫秒）、`created_by` 等；`FacFieldReport` 另含 `synced_at` / `attempts` / `last_error` 支持离线重试。
- **命名**：表 `fac_*` / `sys_*` 前缀；列 `snake_case`；实体用 `@TableField("col_name")` 映射。
- **字符集**：UTF-8（`flyway.encoding=UTF-8`；中文 Windows 下 Python 脚本须显式 `encoding="utf-8"`）。
- **禁止**：SQL 字符串拼接、`${}` 插值、在共享 V 文件后追加 DDL（改用新 V）。

## 5. 迁移评审清单（新增 DDL 走 L3/L4）

1. 新增增量 V 文件（命名合规）；
2. proposal 写明存量影响、回退方案、是否触达共享环境；
3. dev=H2 跑通 `mvn -s ci-settings.xml test`（Flyway 迁移在测试上下文执行）；
4. 若影响契约字段，同步 `frontend-scaffold/docs/api/*.openapi.json` + `check-api-contract.mjs --strict`；
5. 达梦专属方言变更须到 DM8 实例复核（当前环境无法验证，标注待复核）。
