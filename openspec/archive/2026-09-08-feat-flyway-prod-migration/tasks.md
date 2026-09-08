# Tasks: 生产库在线迁移引入 Flyway

- [x] 1. `pom.xml` 引入 `spring-boot-starter-flyway`（托管 9.22.3），确认 H2/PG 内置支持；新增 `dm` profile 声明 `DmJdbcDriver18`（仅 dm 激活解析）。
- [x] 2. `db/migration/h2/V1__init_schema.sql`：复刻现行 6 表全量结构（含 `fac_alarm` 全列 + `alarm_id` 唯一约束）。
- [x] 3. `db/migration/h2/V2__seed_data.sql`：复刻种子（菜单/设备/告警/工位；admin 不进种子）。
- [x] 4. `db/migration/postgresql/V1__init_schema.sql` + `V2__seed_data.sql`：PG 方言（BIGSERIAL/TEXT/BOOLEAN/SMALLINT，种子用 `ON CONFLICT DO NOTHING`）。
- [x] 5. `db/migration/dameng/V1__init_schema.sql` + `V2__seed_data.sql`：DM8 Oracle 兼容方言（NUMBER(19) IDENTITY / VARCHAR2 / CLOB / NUMBER(1) / `FROM dual WHERE NOT EXISTS` 幂等种子）。
- [x] 6. `application.yml` 启用 Flyway（`enabled`/`encoding`/`baseline-on-migrate`）、关闭 `spring.sql.init`、默认 `locations=h2`。
- [x] 7. `application-dev.yml` 移除 `sql.init` 块；`application-prod.yml` 设 `locations=postgresql`。
- [x] 8. 新增 `application-dm.yml`（达梦数据源 + 安全基线 + `locations=dameng` + 驱动 install 文档 + Flyway 达梦兼容缺口说明）。
- [x] 9. 移除被取代的 `schema.sql` / `data.sql` / `db/schema-h2.sql`。
- [x] 10. 更新 README.md / docs/architecture/README.md / AGENTS.md 的 Flyway 双轨与 profile 表。
- [x] 11. 验证：`mvn test` 71 绿；dev(H2) `spring-boot:run` 实跑确认 Flyway 应用 V1+V2、接口种子数据可见、探针 200。

## 备注

- 达梦迁移为评审级，须到达梦实例复核执行（Flyway 社区版无官方达梦模块）。
- 既有生产库存量数据迁移/回填不在本次范围，后续单列 proposal + 回退方案。
