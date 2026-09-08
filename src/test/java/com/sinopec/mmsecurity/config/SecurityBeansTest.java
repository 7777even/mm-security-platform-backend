package com.sinopec.mmsecurity.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * SecurityBeans 启动期密钥强度校验（fail-fast）的纯单元测试（不起 Spring 上下文）。
 * 验证：弱密钥/占位密钥启动即失败；强密钥放行。
 */
class SecurityBeansTest {

    private SecurityBeans buildWith(String jwtSecret, String signatureSecret) throws Exception {
        SecurityBeans beans = new SecurityBeans();
        setField(beans, "jwtSecret", jwtSecret);
        setField(beans, "signatureSecret", signatureSecret);
        return beans;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void shortJwtSecret_throws() throws Exception {
        SecurityBeans beans = buildWith("too-short", "sign-secret-at-least-16-byte");
        assertThrows(IllegalStateException.class, beans::validateSecrets,
                "JWT 密钥 < 256bit 必须启动失败");
    }

    @Test
    void placeholderJwtSecret_throws() throws Exception {
        SecurityBeans beans = buildWith(
                "mm-security-backend-secret-key-change-in-production-min-256-bits",
                "sign-secret-at-least-16-byte");
        assertThrows(IllegalStateException.class, beans::validateSecrets,
                "占位 JWT 密钥必须启动失败");
    }

    @Test
    void shortSignatureSecret_throws() throws Exception {
        SecurityBeans beans = buildWith(
                "dev-secret-not-for-production-please-change-me-please-please", "short");
        assertThrows(IllegalStateException.class, beans::validateSecrets,
                "签名密钥 < 128bit 必须启动失败");
    }

    @Test
    void strongSecrets_passes() throws Exception {
        SecurityBeans beans = buildWith(
                "dev-secret-not-for-production-please-change-me-please-please",
                "dev-sign-secret-not-for-production-please-change-me");
        assertDoesNotThrow(beans::validateSecrets,
                "dev 强密钥应通过校验（保证 dev 上下文可启动）");
    }
}
