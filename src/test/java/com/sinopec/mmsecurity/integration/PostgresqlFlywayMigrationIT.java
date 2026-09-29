package com.sinopec.mmsecurity.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 三方言 Flyway 真机迁移验证 —— PostgreSQL（Testcontainers）。
 *
 * <p>环境前置：本机须运行 Docker daemon（Testcontainers 拉起 postgres 容器）。
 * 若 daemon 未启动，{@code assumeTrue(DockerClientFactory.instance().isDockerAvailable())}
 * 使本测试<b>跳过</b>（不报错），保证 {@code mvn test} 在无容器环境仍绿；daemon 就绪后自动真实运行。</p>
 *
 * <p>验证口径：空 PG → Flyway 应用 classpath:db/migration/postgresql 全部 V 文件 →
 * 断言当前版本非空且已应用迁移数 >= 80（与 h2 方言 V 文件数对齐，捕获方言漂移）。</p>
 */
class PostgresqlFlywayMigrationIT {

    @Test
    void flywayMigratesPostgresqlCleanly() {
        assumeTrue(
            DockerClientFactory.instance().isDockerAvailable(),
            "Docker daemon 未运行：跳过 PG 真机迁移验证（daemon 就绪后自动运行）");

        try (PostgreSQLContainer<?> pg = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("mm_security")
                .withUsername("mm")
                .withPassword("mm")) {
            pg.start();
            Flyway flyway = Flyway.configure()
                    .dataSource(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword())
                    .locations("classpath:db/migration/postgresql")
                    .load();
            int applied = flyway.migrate().migrationsExecuted;
            assertNotNull(flyway.info().current(), "PG 方言迁移应已应用（当前版本非空）");
            assertTrue(applied >= 80,
                    "PG 方言应已应用全部 V 文件（>=80），实际=" + applied);
        }
    }
}
