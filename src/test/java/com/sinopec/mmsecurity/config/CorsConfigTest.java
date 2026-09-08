package com.sinopec.mmsecurity.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * CorsConfig 安全 fail-fast 纯单元测试（不起 Spring 上下文）。
 * 验证：非 dev profile 且 allowed-origins 含 * 时启动抛异常；dev profile 或显式白名单则放行。
 */
class CorsConfigTest {

    private CorsConfig buildWith(String activeProfiles, List<String> allowedOrigins) throws Exception {
        CorsConfig cfg = new CorsConfig();
        Field p = CorsConfig.class.getDeclaredField("activeProfiles");
        p.setAccessible(true);
        p.set(cfg, activeProfiles);
        Field o = CorsConfig.class.getDeclaredField("allowedOrigins");
        o.setAccessible(true);
        o.set(cfg, allowedOrigins);
        return cfg;
    }

    @Test
    void nonDevProfile_withWildcard_throws() throws Exception {
        CorsConfig cfg = buildWith("prod", List.of("*"));
        assertThrows(IllegalStateException.class, cfg::assertNoWildcardInNonDevProfile,
                "prod 配 * 必须启动失败");
    }

    @Test
    void nonDevProfile_withExplicitOrigins_passes() throws Exception {
        CorsConfig cfg = buildWith("dm", List.of("https://app.example.com", "https://admin.example.com"));
        assertDoesNotThrow(cfg::assertNoWildcardInNonDevProfile,
                "dm 配真实域名白名单应放行");
    }

    @Test
    void devProfile_withWildcard_passes() throws Exception {
        CorsConfig cfg = buildWith("dev", List.of("*"));
        assertDoesNotThrow(cfg::assertNoWildcardInNonDevProfile,
                "dev 允许通配（兼容前端 vite 动态端口）");
    }

    @Test
    void devProfileInCommaList_withWildcard_passes() throws Exception {
        CorsConfig cfg = buildWith("local,dev", List.of("*"));
        assertDoesNotThrow(cfg::assertNoWildcardInNonDevProfile,
                "profile 列表含 dev 即视为开发环境");
    }

    @Test
    void prodProfile_withExplicitLocalhost_passes() throws Exception {
        CorsConfig cfg = buildWith("prod", List.of("http://localhost:5173"));
        assertDoesNotThrow(cfg::assertNoWildcardInNonDevProfile,
                "prod 配显式 localhost 白名单（非通配）应放行");
    }

    @Test
    void nonDevProfile_withEmptyOrigins_throws() throws Exception {
        // CORS_ALLOWED_ORIGINS 缺失时解析为空串 -> ['']，不得静默退化为空源
        CorsConfig cfg = buildWith("prod", List.of(""));
        assertThrows(IllegalStateException.class, cfg::assertNoWildcardInNonDevProfile,
                "prod 白名单为空必须启动失败");
    }

    @Test
    void nonDevProfile_withBlankOrigins_throws() throws Exception {
        CorsConfig cfg = buildWith("dm", List.of(" ", "https://app.example.com"));
        assertThrows(IllegalStateException.class, cfg::assertNoWildcardInNonDevProfile,
                "dm 白名单含空白项必须启动失败");
    }
}
