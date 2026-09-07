package com.sinopec.mmsecurity.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JWT 工具：签发 / 解析 / 过期 / 篡改 / 密钥不符 五条路径。
 * 不依赖 Spring 上下文，直接 new + 反射注入 @Value 字段。
 */
class JwtUtilTest {

    private static final String SECRET = "test-secret-at-least-256-bit-long-key-1234567890abcdefghij";
    private static final String OTHER_SECRET = "other-secret-which-is-also-long-enough-1234567890abcdefghijkl";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessTtl", 7200L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTtl", 604800L);
        ReflectionTestUtils.setField(jwtUtil, "issuer", "mm-security-backend");
    }

    @Test
    void issueAccess_then_parse_roundtrip() {
        String token = jwtUtil.issueAccess("admin", "ADMIN");
        Claims claims = jwtUtil.parse(token);
        assertNotNull(claims);
        assertEquals("admin", claims.getSubject());
        assertEquals("access", claims.get("type"));
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void expiredToken_returnsNull() {
        JwtUtil expiredIssuer = new JwtUtil();
        ReflectionTestUtils.setField(expiredIssuer, "secret", SECRET);
        ReflectionTestUtils.setField(expiredIssuer, "accessTtl", -1L); // 签发即过期
        ReflectionTestUtils.setField(expiredIssuer, "refreshTtl", 604800L);
        ReflectionTestUtils.setField(expiredIssuer, "issuer", "mm-security-backend");

        String token = expiredIssuer.issueAccess("admin", "ADMIN");
        assertNull(jwtUtil.parse(token));
    }

    @Test
    void tamperedSignature_returnsNull() {
        String token = jwtUtil.issueAccess("admin", "ADMIN");
        String tampered = token.endsWith("a") ? token.substring(0, token.length() - 1) + "b"
                : token.substring(0, token.length() - 1) + "a";
        assertNull(jwtUtil.parse(tampered));
    }

    @Test
    void wrongSecret_returnsNull() {
        String token = jwtUtil.issueAccess("admin", "ADMIN");
        JwtUtil other = new JwtUtil();
        ReflectionTestUtils.setField(other, "secret", OTHER_SECRET);
        ReflectionTestUtils.setField(other, "accessTtl", 7200L);
        ReflectionTestUtils.setField(other, "refreshTtl", 604800L);
        ReflectionTestUtils.setField(other, "issuer", "mm-security-backend");
        assertNull(other.parse(token));
    }
}
