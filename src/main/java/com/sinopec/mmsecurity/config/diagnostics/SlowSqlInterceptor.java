package com.sinopec.mmsecurity.config.diagnostics;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 慢 SQL 诊断插件（仅当 dev-diag profile 激活时，由 {@code MybatisPlusConfig} 的
 * ConfigurationCustomizer 显式加入 MyBatis 配置）。
 *
 * <p>用标准 MyBatis 插件环绕 Executor 的 query / update，计量真实执行耗时，超过阈值
 * （毫秒，由构造器传入）以 WARN 输出 SQL 文本与映射语句 id，用于本地 dev 排查 N+1、
 * 全表扫描、重聚合等热点。本插件不参与业务、不改执行结果、不改 JDBC 连接（相比
 * p6spy 更轻、零侵入）。</p>
 *
 * <p>为何用 ConfigurationCustomizer 显式 addInterceptor（而非依赖 MyBatis-Plus 自动收集
 * 独立的 Interceptor Bean）：本版本下自动收集在测试上下文不可靠，显式加入既保证
 * dev-diag 下可靠生效，又保证默认 dev / 测试 / prod / dm 完全不挂载（477 基线零影响）。</p>
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        org.apache.ibatis.cache.CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class SlowSqlInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SlowSqlInterceptor.class);

    private final long thresholdMs;

    public SlowSqlInterceptor(long thresholdMs) {
        this.thresholdMs = thresholdMs;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            if (elapsedMs >= thresholdMs) {
                Object[] args = invocation.getArgs();
                MappedStatement ms = (MappedStatement) args[0];
                Object parameter = args[1];
                BoundSql boundSql;
                if (args.length >= 6 && args[5] instanceof BoundSql) {
                    boundSql = (BoundSql) args[5];
                } else {
                    boundSql = ms.getBoundSql(parameter);
                }
                String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
                log.warn("[SLOW-SQL {}ms] {} | SQL: {}", elapsedMs, ms.getId(), sql);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 阈值由构造器传入，无需从 properties 读取。
    }
}
