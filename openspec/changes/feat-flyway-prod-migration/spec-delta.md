# Spec Delta: 生产库在线迁移引入 Flyway

## 变更类型

- 基础设施 / 数据库迁移机制（无 REST API 契约变更）。

## 数据库 Schema 管理

- **Before**：dev 靠 `spring.sql.init` 加载 `schema.sql`/`data.sql`；生产无版本化迁移；`db/schema-h2.sql` 与现行结构分叉。
- **After**：Flyway 接管全部建表与种子；三套方言版本化迁移位于 `db/migration/{h2,postgresql,dameng}/`，按 profile 由 `spring.flyway.locations` 指向。双轨（`V1` 快照 + `V` 增量），已发布 `V` 文件禁止改/删。

## 数据源 / Profile

- `dev` → H2 + `db/migration/h2`
- `prod` → PostgreSQL + `db/migration/postgresql`（兼容/回退）
- `dm` → 达梦 DM8 + `db/migration/dameng`（信创生产选定；新增 profile）

## 接口契约

- 无新增 / 修改 / 删除的 REST 端点。
- `admin` / `admin@2026` 默认账号仍由 `AuthService.ensureAdmin()` 启动时写入（不依赖种子脚本）。

## 兼容性

- 71 单测（Mockito 独立测试，不启 Spring 上下文）不受影响。
- `spring.sql.init.enabled=false` 后，旧的 `schema.sql`/`data.sql` 不再被加载，相关文件已删除。
