package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.config.RateLimitProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimitFilter 行为测试（spring-test Mock，无完整上下文）。
 * 覆盖：超阈值 429 / 放行 actuator / 放行 OPTIONS / 白名单 / 关闭开关 / X-Forwarded-For 信任。
 */
class RateLimitFilterTest {

    private RateLimitFilter build(boolean enabled, int qps, int burst, List<String> white, boolean trustFwd) {
        RateLimitProperties props = new RateLimitProperties();
        props.setEnabled(enabled);
        props.setGlobalQps(qps);
        props.setGlobalBurst(burst);
        props.setTrustForwarded(trustFwd);
        props.setIpWhitelist(white);
        return new RateLimitFilter(props, new ObjectMapper(), new SimpleMeterRegistry());
    }

    @Test
    void returns429WhenExceeded() throws Exception {
        RateLimitFilter f = build(true, 1, 1, List.of(), false);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/alarm/list");
        req.setRemoteAddr("10.0.0.1");
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse r1 = new MockHttpServletResponse();
        f.doFilter(req, r1, chain);
        assertEquals(200, r1.getStatus());

        MockHttpServletResponse r2 = new MockHttpServletResponse();
        f.doFilter(req, r2, chain);
        assertEquals(429, r2.getStatus(), "超过全局桶容量应返回 429");
        assertEquals("1", r2.getHeader("Retry-After"));
    }

    @Test
    void bypassActuator() throws Exception {
        RateLimitFilter f = build(true, 1, 1, List.of(), false);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/actuator/health");
        req.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse r = new MockHttpServletResponse();
        f.doFilter(req, r, new MockFilterChain());
        assertEquals(200, r.getStatus(), "/actuator 探针必须免限流");
    }

    @Test
    void bypassOptions() throws Exception {
        RateLimitFilter f = build(true, 1, 1, List.of(), false);
        MockHttpServletRequest req = new MockHttpServletRequest("OPTIONS", "/api/v1/alarm/list");
        req.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse r = new MockHttpServletResponse();
        f.doFilter(req, r, new MockFilterChain());
        assertEquals(200, r.getStatus(), "OPTIONS 预检必须免限流");
    }

    @Test
    void bypassWhitelist() throws Exception {
        RateLimitFilter f = build(true, 1, 1, List.of("10.0.0.1"), false);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/alarm/list");
        req.setRemoteAddr("10.0.0.1");
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse r = new MockHttpServletResponse();
            f.doFilter(req, r, new MockFilterChain());
            assertEquals(200, r.getStatus(), "白名单 IP 不应被限流");
        }
    }

    @Test
    void bypassWhenDisabled() throws Exception {
        RateLimitFilter f = build(false, 1, 1, List.of(), false);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/alarm/list");
        req.setRemoteAddr("10.0.0.1");
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse r = new MockHttpServletResponse();
            f.doFilter(req, r, new MockFilterChain());
            assertEquals(200, r.getStatus(), "disabled 时不应限流");
        }
    }

    @Test
    void trustsForwardedFor() throws Exception {
        RateLimitFilter f = build(true, 1, 1, List.of("203.0.113.7"), true);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/alarm/list");
        req.setRemoteAddr("10.0.0.9");
        req.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.9");
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse r = new MockHttpServletResponse();
            f.doFilter(req, r, new MockFilterChain());
            assertEquals(200, r.getStatus(), "信任 XFF 时白名单应匹配第一个地址");
        }
    }
}
