# Proposal: 三方言 Flyway 真机迁移验证补齐 Testcontainers IT（flyway-testcontainers-it）

> 状态：`approved` —— 测试基建补全，无对外接口/契约变更。

## Why
此前三方言 Flyway 迁移仅靠 H2 内存单测（`DbLayerIntegrationIT`，复用 Flyway V 文件，`applied>=80`）验证，PG/DM8 真机路径从未实跑，方言漂移（达梦 IDENTITY 语法/保留字、PG 类型）无法被 CI 捕获。本次补齐 Testcontainers 真机 IT：PG 走容器、DM8 走 env 门控，环境就绪即自动真跑，缺位优雅跳过。

## What Changes
- `pom.xml` 新增 testcontainers 1.19.8（test scope）+ junit-jupiter 1.19.8 + postgresql 1.19.8。
- 新增 `integration.PostgresqlFlywayMigrationIT`：`assumeTrue(DockerClientFactory.instance().isDockerAvailable())` 门控；空 PG 应用 `classpath:db/migration/postgresql` 全部 V → 断言 `info().current()` 非空且 `applied>=80`。
- 新增 `integration.DamengFlywayMigrationIT`：`DAMENG_JDBC_URL` env 门控（DM8 无公开镜像，须 -Pdm + 本地驱动 jar + 实例）；应用 `classpath:db/migration/dameng` → 断言同上。
- 既有 `DbLayerIntegrationIT` 已含 H2 迁移基线（`applied>=80`）。

## Impact
- 测试基建，无生产代码/契约变更。
- `mvn test` 在 Docker 缺失环境：PG/DM IT 跳过，全绿；daemon/实例就绪后自动真跑。
