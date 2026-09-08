package com.sinopec.mmsecurity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.security.HmacFilter;
import com.sinopec.mmsecurity.security.JwtFilter;
import com.sinopec.mmsecurity.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全相关 Bean 装配。
 *
 * 过滤器顺序约定（与 AGENTS.md §6.1 对齐）：
 *   CorsFilter(HIGHEST_PRECEDENCE, 见 CorsConfig)
 *     → HmacFilter(HIGHEST_PRECEDENCE+1) → JwtFilter(HIGHEST_PRECEDENCE+10) → 拦截器链
 *
 * CorsFilter 必须最先执行：保证被 HmacFilter/JwtFilter 短路的鉴权响应也带 CORS 头，
 * 否则浏览器会报「No 'Access-Control-Allow-Origin' header」。
 *
 * 不再依赖 Spring Boot 对 @Component Filter 的自动注册（其相对顺序由 bean 名哈希决定，不可控），
 * 改为在此用 FilterRegistrationBean 显式 setOrder，保证：先校验签名，再解析身份。
 */
@Configuration
public class SecurityBeans {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HmacFilter hmacFilter(
            @Value("${signature.enabled}") boolean enabled,
            @Value("${signature.secret}") String secret,
            @Value("${signature.max-skew-seconds}") long maxSkewSeconds,
            ObjectMapper objectMapper) {
        return new HmacFilter(enabled, secret, maxSkewSeconds, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<HmacFilter> hmacFilterRegistration(HmacFilter hmacFilter) {
        FilterRegistrationBean<HmacFilter> bean = new FilterRegistrationBean<>(hmacFilter);
        // +1：让位给 CorsFilter（HIGHEST_PRECEDENCE），CORS 头先写上
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return bean;
    }

    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        return new JwtFilter(jwtUtil, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> bean = new FilterRegistrationBean<>(jwtFilter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return bean;
    }
}
