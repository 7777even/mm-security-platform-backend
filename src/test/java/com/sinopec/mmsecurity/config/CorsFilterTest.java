package com.sinopec.mmsecurity.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CorsFilter 纯单元测试（不起 Spring 上下文）。
 * 验证：CORS 头由 Servlet 级过滤器最先添加，且预检请求被短路。
 * 这是修复「被鉴权过滤器短路的响应缺少 Access-Control-Allow-Origin」的关键保障。
 */
class CorsFilterTest {

    private CorsFilter buildFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.addAllowedOrigin("http://localhost:5174");
        cfg.addAllowedMethod("*");
        cfg.addAllowedHeader("*");
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        return new CorsFilter(source);
    }

    @Test
    void preflightOptions_getsCorsHeaderAndShortCircuits() throws Exception {
        CorsFilter filter = buildFilter();
        MockHttpServletRequest req = new MockHttpServletRequest("OPTIONS", "/api/v1/audit/log");
        req.addHeader("Origin", "http://localhost:5174");
        req.addHeader("Access-Control-Request-Method", "POST");
        MockHttpServletResponse res = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> invoked.set(true);

        filter.doFilter(req, res, chain);

        assertEquals("http://localhost:5174", res.getHeader("Access-Control-Allow-Origin"));
        assertFalse(invoked.get(), "预检请求应由 CorsFilter 短路，不进入后续过滤器链");
    }

    @Test
    void actualRequest_getsCorsHeaderAndContinues() throws Exception {
        CorsFilter filter = buildFilter();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/auth/menus");
        req.addHeader("Origin", "http://localhost:5174");
        MockHttpServletResponse res = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> invoked.set(true);

        filter.doFilter(req, res, chain);

        assertEquals("http://localhost:5174", res.getHeader("Access-Control-Allow-Origin"));
        assertTrue(invoked.get(), "真实请求应继续后续过滤器链（含 JwtFilter）");
    }
}
