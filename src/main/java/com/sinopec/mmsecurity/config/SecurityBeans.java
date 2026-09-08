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

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Set;

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

    /** 已知弱/占位 JWT 密钥（已从 base application.yml 移除默认，此处兜底拦截以防重新引入） */
    private static final Set<String> KNOWN_WEAK_JWT_SECRETS = Set.of(
            "mm-security-backend-secret-key-change-in-production-min-256-bits");
    /** 已知弱/占位签名密钥 */
    private static final Set<String> KNOWN_WEAK_SIGN_SECRETS = Set.of(
            "sign-secret-change-in-production");

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${signature.secret}")
    private String signatureSecret;

    /**
     * 启动期密钥强度校验（fail-fast）：JWT 密钥 ≥ 256bit 且非已知占位；
     * 签名密钥 ≥ 128bit 且非已知占位。任何弱密钥/占位密钥都会被拦截，杜绝带着弱密钥上线。
     */
    @PostConstruct
    public void validateSecrets() {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "jwt.secret 长度必须 ≥ 256 bit（32 字节）。生产须通过环境变量 JWT_SECRET 注入强随机密钥，禁止弱密钥。");
        }
        if (KNOWN_WEAK_JWT_SECRETS.contains(jwtSecret)) {
            throw new IllegalStateException(
                    "jwt.secret 使用了已知占位密钥，禁止用于生产。请通过 JWT_SECRET 注入真实强密钥。");
        }
        if (signatureSecret == null || signatureSecret.getBytes(StandardCharsets.UTF_8).length < 16) {
            throw new IllegalStateException(
                    "signature.secret 长度必须 ≥ 128 bit（16 字节）。生产须通过 SIGNATURE_SECRET 注入强随机密钥。");
        }
        if (KNOWN_WEAK_SIGN_SECRETS.contains(signatureSecret)) {
            throw new IllegalStateException(
                    "signature.secret 使用了已知占位密钥，禁止用于生产。请通过 SIGNATURE_SECRET 注入真实强密钥。");
        }
    }

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
