package com.sinopec.mmsecurity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.common.TracingFilter;
import com.sinopec.mmsecurity.config.RateLimitProperties;
import com.sinopec.mmsecurity.security.HmacFilter;
import com.sinopec.mmsecurity.security.RateLimitFilter;
import com.sinopec.mmsecurity.security.JwtFilter;
import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.TokenVersionService;
import io.micrometer.tracing.Tracer;
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
 *     → TracingFilter(HIGHEST_PRECEDENCE, 见下) → HmacFilter(HIGHEST_PRECEDENCE+1)
 *     → JwtFilter(HIGHEST_PRECEDENCE+10) → 拦截器链
 *
 * CorsFilter 必须最先执行：保证被 HmacFilter/JwtFilter 短路的鉴权响应也带 CORS 头，
 * 否则浏览器会报「No 'Access-Control-Allow-Origin' header」。
 *
 * TracingFilter 紧随 CorsFilter（同 HIGHEST_PRECEDENCE），确定性早于 HmacFilter/JwtFilter，
 * 使所有下游过滤器与业务日志、Result.traceId 在请求进入业务前即拿到统一 traceId（详见 TracingFilter）。
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

    /**
     * 必须走三参构造注入 TokenVersionService，否则登出无法让旧令牌失效
     * （两参构造只在测试里用，会跳过版本校验）。
     */
    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, TokenVersionService tokenVersionService) {
        return new JwtFilter(jwtUtil, objectMapper, tokenVersionService);
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> bean = new FilterRegistrationBean<>(jwtFilter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return bean;
    }

    /**
     * 分布式链路追踪根 span 过滤器（OpenTelemetry / Micrometer Tracing 桥接）。
     * 依赖 Micrometer Tracing 自动配置的 Tracer bean（引入 tracing 依赖后由 Spring Boot 托管）。
     * 顺序 HIGHEST_PRECEDENCE：与 CorsFilter 同优先级、确定性早于 HmacFilter(+1)/JwtFilter(+10)，
     * 保证下游过滤器与业务日志、Result.traceId 拿到统一 traceId。
     */
    @Bean
    public TracingFilter tracingFilter(Tracer tracer) {
        return new TracingFilter(tracer);
    }

    @Bean
    public FilterRegistrationBean<TracingFilter> tracingFilterRegistration(TracingFilter tracingFilter) {
        FilterRegistrationBean<TracingFilter> bean = new FilterRegistrationBean<>(tracingFilter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /**
     * 接口级限流防刷过滤器（进程内令牌桶，零外部依赖）。
     * 顺序 HIGHEST_PRECEDENCE + 2：位于 HmacFilter(+1) 之后、JwtFilter(+10) 之前。
     * 仅对通过 HMAC 签名的合法流量限速——无有效签名的裸请求已在 Hmac 层被 401 挡掉，不消耗限流配额。
     * 放行规则与 HmacFilter 对齐（/actuator、/h2-console、OPTIONS）。
     */
    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitProperties rateLimitProperties, ObjectMapper objectMapper,
                                          io.micrometer.core.instrument.MeterRegistry meterRegistry) {
        return new RateLimitFilter(rateLimitProperties, objectMapper, meterRegistry);
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter rateLimitFilter) {
        FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>(rateLimitFilter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return bean;
    }
}
