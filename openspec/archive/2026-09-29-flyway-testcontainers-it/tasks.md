# Tasks

## 1. 依赖
- [x] `pom.xml` 加 testcontainers/junit-jupiter/postgresql 1.19.8（test scope）

## 2. 真机 IT
- [x] `integration.PostgresqlFlywayMigrationIT`：Docker 门控，空 PG 应用 postgresql 方言 V → applied>=80
- [x] `integration.DamengFlywayMigrationIT`：`DAMENG_JDBC_URL` env 门控，应用 dameng 方言 V → applied>=80
- [x] 修正 `DockerClientFactory` 导入包（`org.testcontainers` 非 `org.testcontainers.utility`）

## 3. 验证
- [x] 编译+跳过验证：`mvn test -Dtest=PostgresqlFlywayMigrationIT,DamengFlywayMigrationIT -Djacoco.skip=true` → BUILD SUCCESS，2 例均 SKIPPED
- [x] H2 基线：`DbLayerIntegrationIT.flywayMigrations_allH2VersionsApplied_noDrift` 通过（applied>=80）

## 4. 归档
- [x] `git mv` 到 `openspec/archive/2026-09-29-flyway-testcontainers-it`
- [x] `node scripts/check-openspec-hygiene.mjs` 通过
