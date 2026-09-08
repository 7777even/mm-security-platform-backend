package com.sinopec.mmsecurity.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件链。
 *
 * 方言不再写死 DbType.POSTGRE_SQL：使用无参构造后，MyBatis-Plus 会依据 JDBC 连接元数据
 * 自动判定数据库类型（dev 的 H2、生产 PostgreSQL 均为主流驱动，可正确识别），
 * 避免 dev 下生成 PostgreSQL 专属分页 SQL 导致 H2 报错。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 乐观锁：对带 @Version 标记的实体，在 update 时自动附加 WHERE version = ? 并自增。
        // 必须位于分页拦截器之前，确保版本条件在分页改写前注入。
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
