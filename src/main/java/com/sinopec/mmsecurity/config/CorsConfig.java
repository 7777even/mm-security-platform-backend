package com.sinopec.mmsecurity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * Servlet 级 CORS 过滤器配置。
 *
 * <p>为什么用 Servlet 级过滤器而不是 WebMvcConfigurer.addCorsMappings：</p>
 * <ul>
 *   <li>addCorsMappings 的 CORS 头由 DispatcherServlet 内的 CorsInterceptor 添加，
 *       位于 JwtFilter/HmacFilter（Servlet 过滤器，HIGHEST_PRECEDENCE）之后；</li>
 *   <li>当请求缺合法 token 时，JwtFilter 在到达 DispatcherServlet 之前就抛异常，
 *       异常冒泡给 Tomcat 产生 500，且响应没有 CORS 头，
 *       浏览器表现为「No 'Access-Control-Allow-Origin' header」CORS 阻断；</li>
 *   <li>Servlet 级 CorsFilter 在{@code HIGHEST_PRECEDENCE} 最先执行，
 *       给【所有】响应（含被 JwtFilter 短路的 401）都加上 CORS 头，从根上消除该问题。</li>
 * </ul>
 *
 * 顺序：CorsFilter(HIGHEST_PRECEDENCE) → HmacFilter(HIGHEST_PRECEDENCE+1) →
 *       JwtFilter(HIGHEST_PRECEDENCE+10) → 拦截器链。
 *
 * <p>安全 fail-fast：CORS 通配(*) 仅允许 dev profile。任何非 dev profile（prod/dm 等）若
 * 把 {@code app.cors.allowed-origins} 配成含 {@code *}，应用启动即失败——杜绝「生产误配通配、
 * 任意站点可带凭据跨域打本服务」的灾难。生产必须显式注入真实前端域名白名单。</p>
 */
@Configuration
public class CorsConfig {

    /** 当前激活的 profile（用于判断是否为 dev，决定能否接受通配来源） */
    @Value("${spring.profiles.active:dev}")
    private String activeProfiles;

    /** 来源白名单；缺省为前端脚手架 dev 端口，生产须由 application-prod.yml 注入真实域名 */
    @Value("#{'${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:5175,http://localhost:5180,http://localhost:4173}'.split(',')}")
    private List<String> allowedOrigins;

    /**
     * 启动期安全校验：非 dev profile 禁止 CORS 通配。
     * 触发条件：prod/dm 等环境 {@code app.cors.allowed-origins} 含 {@code *}。
     * 抛异常让 Spring 上下文初始化失败，fail-fast 早于任何请求。
     */
    @PostConstruct
    public void assertNoWildcardInNonDevProfile() {
        boolean isDev = Arrays.stream(activeProfiles.split(","))
                .map(String::trim)
                .anyMatch("dev"::equalsIgnoreCase);
        if (!isDev && allowedOrigins.contains("*")) {
            throw new IllegalStateException(
                    "CORS 通配(*) 仅允许 dev profile；当前 profile=[" + activeProfiles
                            + "] 禁止通配。请在 application-" + activeProfiles
                            + ".yml 通过 CORS_ALLOWED_ORIGINS 注入真实前端域名白名单（逗号分隔，不含 *）。");
        }
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        boolean wildcard = allowedOrigins.contains("*");
        CorsConfiguration cfg = new CorsConfiguration();
        if (wildcard) {
            // 通配来源 + 凭据：必须用 allowedOriginPatterns（反射真实 origin），不能用 allowedOrigins("*")
            cfg.addAllowedOriginPattern("*");
        } else {
            allowedOrigins.forEach(cfg::addAllowedOrigin);
        }
        cfg.addAllowedMethod("*");
        cfg.addAllowedHeader("*");
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        source.registerCorsConfiguration("/ws/**", cfg);
        return source;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean<CorsFilter> bean =
                new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
        // 必须最先执行，早于 HmacFilter/JwtFilter，才能给被短路的鉴权响应也加上 CORS 头
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
