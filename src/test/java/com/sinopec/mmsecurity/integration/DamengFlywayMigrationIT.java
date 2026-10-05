package com.sinopec.mmsecurity.integration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
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
        flyway.migrate();

        // 注意：这里断言的是「库当前所处版本」，而不是「本次应用了几个迁移」。
        // 达梦走的是本机持久实例（非一次性容器），重复跑时 V1..Vn 早已应用，
        // 本次 migrationsExecuted 只会是新增的那几个（例如 23），用应用数做断言会误判。
        MigrationInfo current = flyway.info().current();
        assertNotNull(current, "达梦方言迁移应已应用（当前版本非空）");
        if (current == null) {
            return;
        }
        int version = Integer.parseInt(current.getVersion().getVersion());
        assertTrue(version >= 80,
                "达梦方言当前版本应 >= 80（V 文件总数 104），实际=" + current.getVersion());
    }
}
