package com.sinopec.mmsecurity.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.sinopec.mmsecurity.config.diagnostics.SlowSqlInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * MyBatis-Plus 插件链。
 *
 * 方言不再写死 DbType.POSTGRE_SQL：使用无参构造后，MyBatis-Plus 会依据 JDBC 连接元数据
 * 自动判定数据库类型（dev 的 H2、生产 PostgreSQL 均为主流驱动，可正确识别），
 * 避免 dev 下生成 PostgreSQL 专属分页 SQL 导致 H2 报错。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 插件链（InnerInterceptor）。
     *
     * 乐观锁：对带 @Version 标记的实体，在 update 时自动附加 WHERE version = ? 并自增。
     * 必须位于分页拦截器之前，确保版本条件在分页改写前注入。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 慢 SQL 诊断插件：仅在 dev-diag profile 激活时注册为 MyBatis 插件 Bean。
     * MyBatis-Plus 自动配置会收集所有 {@code Interceptor} Bean 并加入 SqlSessionFactory
     * 配置；默认 dev / 测试 / prod / dm 不激活该 profile，故完全不挂载，477 基线零影响。
     * 启用方式（本地 dev）：SPRING_PROFILES_ACTIVE=dev,dev-diag（环境变量 / 启动参数，
     * 优先级高于 application.yml 的 active: dev）。阈值由 application-dev-diag.yml 设为 50ms。
     */
    @Bean
    @Profile("dev-diag")
    public SlowSqlInterceptor slowSqlInterceptor(Environment environment) {
        long thresholdMs = environment.getProperty(
                "mm.diagnostics.slow-sql.threshold-ms", Long.class, 50L);
        return new SlowSqlInterceptor(thresholdMs);
    }
}
