package com.sinopec.mmsecurity.config;

import com.sinopec.mmsecurity.security.HardControlInterceptor;
import com.sinopec.mmsecurity.security.RequireAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册拦截器链 + CORS。
 *
 * 拦截器顺序影响安全检查流：
 *   1. HardControlInterceptor —— 硬控路径兜底（最先挡住非法下行）
 *   2. RequireAuthInterceptor —— 鉴权（确保硬控请求也有身份）
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final HardControlInterceptor hardControlInterceptor;
    private final RequireAuthInterceptor requireAuthInterceptor;

    /** 前端脚手架 dev 端口，允许跨域 */
    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5180",
            "http://localhost:4173"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hardControlInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(requireAuthInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        registry.addMapping("/ws/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true);
    }
}
