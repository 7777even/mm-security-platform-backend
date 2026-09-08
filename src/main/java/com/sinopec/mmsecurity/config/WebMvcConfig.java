package com.sinopec.mmsecurity.config;

import com.sinopec.mmsecurity.security.HardControlInterceptor;
import com.sinopec.mmsecurity.security.RequireAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：仅注册拦截器链。
 *
 * <p>CORS 已迁移到 {@link CorsConfig} 的 Servlet 级 CorsFilter（先于 JwtFilter/HmacFilter 执行），
 * 不再用 WebMvcConfigurer.addCorsMappings——否则被鉴权过滤器短路的响应会缺少 CORS 头，
 * 浏览器报「No 'Access-Control-Allow-Origin' header」。</p>
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hardControlInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(requireAuthInterceptor).addPathPatterns("/api/**");
    }
}
