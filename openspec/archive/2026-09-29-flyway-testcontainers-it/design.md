# Design: 三方言 Flyway 真机迁移验证

## ADR-1：PG 用 Testcontainers，DM8 用 env 门控
PG 有官方 `postgres` 镜像，直接 `PostgreSQLContainer("postgres:16-alpine")` + `assumeTrue(DockerClientFactory...)` 优雅跳过。DM8 无公开镜像/容器模块，驱动须 `-Pdm` + 本地 `install-file`，故仅 `DAMENG_JDBC_URL` 显式提供时运行。

## ADR-2：验证口径统一
三种方言均「空库 → Flyway 应用各自 `classpath:db/migration/<方言>` 全部 V 文件 → 断言当前版本非空 + `applied>=80`」，与 h2 文件数对齐，捕获方言漂移（达梦 IDENTITY/保留字、PG 类型）。

## ADR-3：不破坏既有门禁
仅新增 IT 类（surefire 按 `-Dtest` 显式选取；CI `mvn test` 默认含 `*IT`），缺位即跳过，不拖慢/不拖红日常单测；jacoco 覆盖率由既有单测维持。
