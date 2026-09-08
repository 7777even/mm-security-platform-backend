package com.sinopec.mmsecurity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.common.ResultCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * JwtFilter 纯单元测试（不起 Spring 上下文）。
 * 验证：鉴权失败不再抛异常冒泡成 500，而是直接写出 HTTP 401 + B3 包络。
 */
class JwtFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JwtFilter filterWith(JwtUtil jwtUtil) {
        return new JwtFilter(jwtUtil, objectMapper);
    }

    @Test
    void noAuthorizationHeader_returns401B3_andShortCircuits() throws Exception {
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        JwtFilter filter = filterWith(jwtUtil);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/audit/log");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), res.getStatus(), "缺 token 应返回 401，而非冒泡成 500");
        String body = res.getContentAsString();
        assertTrue(body.contains("\"code\":" + ResultCode.TOKEN_INVALID), "响应体应为 B3 包络，含 TOKEN_INVALID 码");
        assertTrue(body.contains("缺少 Authorization 头"));
        assertNull(chain.getRequest(), "鉴权失败应短路，不进入后续过滤器链");
    }

    @Test
    void invalidToken_returns401B3() throws Exception {
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        when(jwtUtil.parse(anyString())).thenReturn(null); // 令牌无效/过期
        JwtFilter filter = filterWith(jwtUtil);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/audit/log");
        req.addHeader("Authorization", "Bearer bad.token.value");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), res.getStatus());
        assertTrue(res.getContentAsString().contains("\"code\":" + ResultCode.TOKEN_INVALID));
        assertNull(chain.getRequest());
    }

    @Test
    void optionsPreflight_bypassesAuthAndContinues() throws Exception {
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        JwtFilter filter = filterWith(jwtUtil);
        MockHttpServletRequest req = new MockHttpServletRequest("OPTIONS", "/api/v1/audit/log");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNotNull(chain.getRequest(), "OPTIONS 预检必须放行，直达后续链（含 CORS）");
        assertEquals(HttpStatus.OK.value(), res.getStatus());
    }

    @Test
    void validAccessToken_continuesChain() throws Exception {
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        Claims claims = Mockito.mock(Claims.class);
        when(claims.get("type", String.class)).thenReturn("access");
        when(claims.getSubject()).thenReturn("alice");
        when(jwtUtil.parse(anyString())).thenReturn(claims);

        JwtFilter filter = filterWith(jwtUtil);
        // 用受保护端点（非白名单）验证 token 解析 + UserContext 注入；
        // 注意 auth/menus 与 auth/me 已加入免鉴权白名单，不能用于本用例
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/devices");
        req.addHeader("Authorization", "Bearer valid.token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        // UserContext 在过滤器 finally 中会被清除，需在链执行期间捕获
        java.util.concurrent.atomic.AtomicReference<String> capturedUser =
                new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicBoolean invoked =
                new java.util.concurrent.atomic.AtomicBoolean(false);
        FilterChain chain = (r, s) -> {
            invoked.set(true);
            capturedUser.set(UserContext.get() != null ? UserContext.get().getUsername() : null);
        };

        filter.doFilter(req, res, chain);

        assertTrue(invoked.get(), "合法 token 应通过鉴权并继续过滤器链");
        assertEquals(HttpStatus.OK.value(), res.getStatus(), "合法请求不应被 401");
        assertEquals("alice", capturedUser.get(), "登录态应注入 UserContext 并传入后续链");
    }
}
