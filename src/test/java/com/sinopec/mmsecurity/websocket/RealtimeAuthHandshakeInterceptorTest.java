package com.sinopec.mmsecurity.websocket;

import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.TokenVersionService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RealtimeAuthHandshakeInterceptor 鉴权逻辑测试（纯 Mockito）：覆盖有效令牌 / 无效令牌 /
 * 类型非法 / 版本失效 / 缺令牌 五类路径。
 */
class RealtimeAuthHandshakeInterceptorTest {

    private JwtUtil jwtUtil;
    private TokenVersionService tokenVersionService;
    private RealtimeAuthHandshakeInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        tokenVersionService = mock(TokenVersionService.class);
        interceptor = new RealtimeAuthHandshakeInterceptor(jwtUtil, tokenVersionService);
    }

    private ServerHttpRequest requestWithToken(String token) {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        when(req.getURI()).thenReturn(URI.create("ws://x/ws/alarm" + (token != null ? "?token=" + token : "")));
        return req;
    }

    private Claims validClaims() {
        Claims claims = mock(Claims.class);
        when(claims.get("type", String.class)).thenReturn("access");
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(claims.get("ver")).thenReturn(0);
        return claims;
    }

    @Test
    @DisplayName("有效 access 令牌：返回 true 并注入 LoginUser")
    void validToken_acceptsAndBindsIdentity() {
        Claims claims = validClaims();
        when(jwtUtil.parse("valid")).thenReturn(claims);
        when(tokenVersionService.current("admin")).thenReturn(0);

        Map<String, Object> attrs = new HashMap<>();
        boolean ok = interceptor.beforeHandshake(
                requestWithToken("valid"), mock(ServerHttpResponse.class), mock(WebSocketHandler.class), attrs);

        assertTrue(ok);
        assertInstanceOf(LoginUser.class, attrs.get(RealtimeAuthHandshakeInterceptor.LOGIN_USER_KEY));
        LoginUser u = (LoginUser) attrs.get(RealtimeAuthHandshakeInterceptor.LOGIN_USER_KEY);
        assertTrue("admin".equals(u.getUsername()));
    }

    @Test
    @DisplayName("无效令牌（parse 返回 null）：拒绝")
    void invalidToken_rejected() {
        when(jwtUtil.parse("bad")).thenReturn(null);
        boolean ok = interceptor.beforeHandshake(
                requestWithToken("bad"), mock(ServerHttpResponse.class), mock(WebSocketHandler.class), new HashMap<>());
        assertFalse(ok);
    }

    @Test
    @DisplayName("令牌类型非法（refresh）：拒绝")
    void wrongType_rejected() {
        Claims claims = validClaims();
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(jwtUtil.parse("r")).thenReturn(claims);
        boolean ok = interceptor.beforeHandshake(
                requestWithToken("r"), mock(ServerHttpResponse.class), mock(WebSocketHandler.class), new HashMap<>());
        assertFalse(ok);
    }

    @Test
    @DisplayName("令牌已失效（版本号不符）：拒绝")
    void staleToken_rejected() {
        Claims claims = validClaims();
        when(jwtUtil.parse("old")).thenReturn(claims);
        when(tokenVersionService.current("admin")).thenReturn(1); // 当前版本已前进
        boolean ok = interceptor.beforeHandshake(
                requestWithToken("old"), mock(ServerHttpResponse.class), mock(WebSocketHandler.class), new HashMap<>());
        assertFalse(ok);
    }

    @Test
    @DisplayName("缺令牌查询参数：拒绝")
    void missingToken_rejected() {
        boolean ok = interceptor.beforeHandshake(
                requestWithToken(null), mock(ServerHttpResponse.class), mock(WebSocketHandler.class), new HashMap<>());
        assertFalse(ok);
    }
}
