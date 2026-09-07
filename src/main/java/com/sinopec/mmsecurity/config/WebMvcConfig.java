package com.sinopec.mmsecurity.config;

import com.sinopec.mmsecurity.security.HardControlInterceptor;
import com.sinopec.mmsecurity.security.RequireAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web MVC 配置：注册拦截器链 + CORS。
 *
 * 拦截器顺序影响安全检查流：
 *   1. HardControlInterceptor —— 硬控路径兜底（最先挡住非法下行）
 *   2. RequireAuthInterceptor —— 鉴权（确保硬控请求也有身份）
 *
 * CORS 收敛：来源来自 {@code app.cors.allowed-origins} 白名单，不再使用通配 *。
 * 仅当白名单显式含 * 时才退化到 allowedOriginPatterns("*")（生产不应出现）。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final HardControlInterceptor hardControlInterceptor;
    private final RequireAuthInterceptor requireAuthInterceptor;

    /** 来源白名单；缺省为前端脚手架 dev 端口，生产须由 application-prod.yml 注入真实域名 */
    @Value("#{'${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:5180,http://localhost:4173}'.split(',')}")
    private List<String> allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hardControlInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(requireAuthInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        boolean wildcard = allowedOrigins.contains("*");
        String[] origins = allowedOrigins.toArray(new String[0]);

        if (wildcard) {
            registry.addMapping("/api/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            registry.addMapping("/ws/**")
                    .allowedOriginPatterns("*")
                    .allowCredentials(true);
        } else {
            registry.addMapping("/api/**")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            registry.addMapping("/ws/**")
                    .allowedOrigins(origins)
                    .allowCredentials(true);
        }
        // 防御性 assert：避免误提交导致白名单为空仍放行 localhost
        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException("app.cors.allowed-origins 为空，CORS 白名单未配置");
        }
    }
}
