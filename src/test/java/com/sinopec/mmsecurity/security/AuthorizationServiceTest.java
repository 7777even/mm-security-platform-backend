package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AuthorizationService 越权校验单元测试（不起 Spring 上下文，手动驱动 UserContext）。
 */
class AuthorizationServiceTest {

    private final AuthorizationService service = new AuthorizationService();

    @AfterEach
    void clear() {
        UserContext.clear();
    }

    private void asUser(String username, String role) {
        UserContext.set(new LoginUser(null, username, role));
    }

    @Test
    void assertAdmin_adminPasses() {
        asUser("admin", "ADMIN");
        assertDoesNotThrow(service::assertAdmin);
    }

    @Test
    void assertAdmin_nonAdmin_throws() {
        asUser("alice", "USER");
        assertThrows(BusinessException.class, service::assertAdmin, "非管理员应被拒");
    }

    @Test
    void assertSelfOrAdmin_selfPasses() {
        asUser("alice", "USER");
        assertDoesNotThrow(() -> service.assertSelfOrAdmin("alice"));
    }

    @Test
    void assertSelfOrAdmin_nullTargetPasses() {
        asUser("alice", "USER");
        // 未声明归属：放行，由调用方绑定为本人（现场回传常见场景）
        assertDoesNotThrow(() -> service.assertSelfOrAdmin(null));
    }

    @Test
    void assertSelfOrAdmin_otherUser_throws() {
        asUser("alice", "USER");
        assertThrows(BusinessException.class, () -> service.assertSelfOrAdmin("bob"),
                "普通用户访问他人资源应被拒（水平越权）");
    }

    @Test
    void assertSelfOrAdmin_adminCrossUserPasses() {
        asUser("admin", "ADMIN");
        assertDoesNotThrow(() -> service.assertSelfOrAdmin("bob"), "管理员可跨用户访问");
    }
}
