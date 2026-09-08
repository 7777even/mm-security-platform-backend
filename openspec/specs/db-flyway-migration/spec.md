# db-flyway-migration Specification

## Purpose

数据库版本化迁移机制（Flyway 双轨三方言）。由 Change `feat-flyway-prod-migration`（已归档）回填。

## Requirements

### Requirement: Flyway 接管全部建表与种子

数据库结构一律由 Flyway 版本化迁移管理，禁止回退到 `spring.sql.init` 加载 `schema.sql` / `data.sql` 的机制（`spring.sql.init.enabled=false`）。

#### Scenario: 空库启动

- **WHEN** 任一 profile 以空数据库启动
- **THEN** Flyway 依次执行该方言目录下的 `V1` 快照与全部 `V` 增量，建表并写入种子

### Requirement: 双轨迁移（V1 快照 + V 增量）

`V1__init_schema.sql` 为完整快照（空库直达最新结构），后续 DDL 一律新增版本化增量 `V<版本>__<lower_snake_case>.sql`。

#### Scenario: 已发布 V 文件不可变

- **WHEN** 需要修改已进入共享环境的表结构
- **THEN** 只能新增 `V` 增量文件；修改、重命名或删除已发布的 `V` 文件被禁止

### Requirement: 三方言迁移目录按 profile 指向

版本化迁移按方言分目录维护，由 `spring.flyway.locations` 按 profile 指向。

#### Scenario: profile 与迁移目录映射

- **WHEN** 分别以 `dev` / `prod` / `dm` profile 启动
- **THEN** 分别使用 `db/migration/h2`（H2）、`db/migration/postgresql`（PostgreSQL）、`db/migration/dameng`（达梦 DM8）目录下的迁移

#### Scenario: 三库结构一致

- **WHEN** 新增涉及结构变化的迁移
- **THEN** 三套方言目录同步新增对应增量，列定义保持一致（达梦按 Oracle 兼容方言编写并须在达梦实例复核）

### Requirement: 默认账号不依赖种子脚本

默认管理员账号由应用启动逻辑写入，不依赖迁移种子。

#### Scenario: 启动写入默认账号

- **WHEN** 应用启动
- **THEN** `AuthService.ensureAdmin()` 写入默认账号 `admin` / `admin@2026`
