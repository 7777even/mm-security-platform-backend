package com.sinopec.mmsecurity.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 三方言 Flyway 真机迁移验证 —— 达梦 DM8。
 *
 * <p>DM8 无公开 Testcontainers 镜像/容器模块，且驱动（DmJdbcDriver18）需 {@code -Pdm} profile +
 * 本地 {@code mvn install:install-file} 注入。故本测试以环境变量 {@code DAMENG_JDBC_URL} 门控：
 * 仅当显式提供达梦连接串（如 CI 特配达梦实例）时才运行，否则<b>跳过</b>。</p>
 *
 * <p>验证口径：对给定达梦库 Flyway 应用 classpath:db/migration/dameng 全部 V 文件 →
 * 断言当前版本非空且已应用迁移数 >= 80。</p>
 */
class DamengFlywayMigrationIT {

    @Test
    void flywayMigratesDamengCleanly() {
        String url = System.getenv("DAMENG_JDBC_URL");
        assumeTrue(url != null && !url.isBlank(),
                "未配置 DAMENG_JDBC_URL：跳过达梦真机迁移验证（须 -Pdm + 本地驱动 jar + 达梦实例）");

        Flyway flyway = Flyway.configure()
                .dataSource(url,
                        System.getenv().getOrDefault("DAMENG_JDBC_USER", "SYSDBA"),
                        System.getenv().getOrDefault("DAMENG_JDBC_PASSWORD", "SYSDBA"))
                .locations("classpath:db/migration/dameng")
                .load();
        int applied = flyway.migrate().migrationsExecuted;
        assertNotNull(flyway.info().current(), "达梦方言迁移应已应用（当前版本非空）");
        assertTrue(applied >= 80, "达梦方言应已应用全部 V 文件（>=80），实际=" + applied);
    }
}
