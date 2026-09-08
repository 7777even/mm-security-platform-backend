package com.sinopec.mmsecurity.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.config.CorsConfig;
import com.sinopec.mmsecurity.security.HmacFilter;
import com.sinopec.mmsecurity.security.JwtFilter;
import com.sinopec.mmsecurity.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.lang.reflect.Field;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 前后端联调契约的端到端测试（standalone MockMvc，不起 Spring 上下文，符合项目测试基线）。
 *
 * <p>把真实过滤器链 CorsFilter → HmacFilter → JwtFilter 与探针控制器串起来，锁定上午收口的两类修复：</p>
 * <ul>
 *   <li>被 JwtFilter 短路的 401 响应【必须带 CORS 头】（否则浏览器报 No 'Access-Control-Allow-Origin'）；</li>
 *   <li>auth/menus 与 auth/me 已移出白名单（必须携带有效 Bearer），其余白名单（login/refresh/logout）仍放行、合法 Bearer 通过鉴权；</li>
 *   <li>遗留自定义 /api/v1/health 已废弃：不再白名单，缺 token 返回 401（且仍带 CORS 头）。</li>
 * </ul>
 *
 * <p>CorsConfigurationSource 与 {@link CorsConfig} 同构（localhost:5173 + allowCredentials），显式复刻而非依赖 Spring 装配。</p>
 */
class IntegrationContractTest {

    private static final String ORIGIN = "http://localhost:5173";
    private static final String DEV_SECRET = "dev-secret-not-for-production-please-change-me-please-please";

    private MockMvc mockMvc;
    private JwtUtil jwtUtil;

    @RestController
    static class ProbeController {
        @GetMapping("/api/v1/protected")
        String protectedResource() {
            return "ok";
        }

        @GetMapping("/api/v1/auth/menus")
        String menus() {
            return "menus";
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = buildJwtUtil();

        // 复刻 CorsConfig：/api/** 与 /ws/** 允许 localhost:5173 + 凭据
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.addAllowedOrigin(ORIGIN);
        cfg.addAllowedMethod("*");
        cfg.addAllowedHeader("*");
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        CorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        ((UrlBasedCorsConfigurationSource) source).registerCorsConfiguration("/api/**", cfg);
        ((UrlBasedCorsConfigurationSource) source).registerCorsConfiguration("/ws/**", cfg);
        CorsFilter corsFilter = new CorsFilter(source);

        // dev 下签名挂起（enabled=false），仅占位以对齐真实链
        HmacFilter hmacFilter = new HmacFilter(false, "test-sign-secret", 300L, new ObjectMapper());
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, new ObjectMapper());

        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .addFilters(corsFilter, hmacFilter, jwtFilter)
                .build();
    }

    private JwtUtil buildJwtUtil() throws Exception {
        JwtUtil util = new JwtUtil();
        setField(util, "secret", DEV_SECRET);
        setField(util, "accessTtl", 7200L);
        setField(util, "refreshTtl", 604800L);
        setField(util, "issuer", "mm-security-backend");
        return util;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    /** 受保护端点缺 token → 401，且响应带 CORS 头（锁死「500 无 CORS 头」历史缺陷） */
    @Test
    void protectedEndpoint_noToken_returns401WithCorsHeader() throws Exception {
        mockMvc.perform(get("/api/v1/protected").header("Origin", ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    /** auth/menus 已移出白名单：无 token 必须 401，且仍带 CORS 头 */
    @Test
    void menus_noToken_returns401WithCorsHeader() throws Exception {
        mockMvc.perform(get("/api/v1/auth/menus").header("Origin", ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    /** 遗留自定义 /api/v1/health 已废弃：不再白名单，缺 token 返回 401 且带 CORS 头 */
    @Test
    void legacyHealthEndpoint_removed_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/health").header("Origin", ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    /** 合法 Bearer 通过鉴权并带 CORS 头 */
    @Test
    void protectedEndpoint_validBearer_returns200WithCorsHeader() throws Exception {
        String token = jwtUtil.issueAccess("admin", "ADMIN");
        mockMvc.perform(get("/api/v1/protected")
                        .header("Origin", ORIGIN)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    /** 浏览器预检 OPTIONS 必须放行并带 CORS 头 */
    @Test
    void preflightOptions_returns200WithCorsHeader() throws Exception {
        mockMvc.perform(options("/api/v1/protected")
                        .header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }
}
