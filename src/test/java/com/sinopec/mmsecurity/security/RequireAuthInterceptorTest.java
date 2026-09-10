package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RequireAuthInterceptor（纯 Mockito）：value / role / perm 三级门禁语义。
 * <ul>
 *   <li>未登录 → 401；role 不符 → 403；perm 不持有 → 403；全满足 → 放行。</li>
 * </ul>
 */
class RequireAuthInterceptorTest {

    private final RoleAuthorityService roleAuthority = mock(RoleAuthorityService.class);
    private final RequireAuthInterceptor interceptor = new RequireAuthInterceptor(roleAuthority);

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void noAnnotation_passes() throws Exception {
        assertDoesNotThrow(() -> interceptor.preHandle(request, response, handler("open")));
    }

    @Test
    void missingLogin_returns401() throws Exception {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, response, handler("adminOnly")));
        assertEquals(ResultCode.UNAUTHORIZED, ex.getCode());
    }

    @Test
    void roleMismatch_returns403() throws Exception {
        UserContext.set(new LoginUser(2L, "viewer", "USER"));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, response, handler("adminOnly")));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void roleMatch_passes() throws Exception {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        assertDoesNotThrow(() -> interceptor.preHandle(request, response, handler("adminOnly")));
    }

    @Test
    void permMissing_returns403() throws Exception {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        when(roleAuthority.hasPerm(anyString(), anyString())).thenReturn(false);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, response, handler("permOnly")));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void permHeld_passes() throws Exception {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        when(roleAuthority.hasPerm("ADMIN", "system:user:create")).thenReturn(true);
        assertDoesNotThrow(() -> interceptor.preHandle(request, response, handler("permOnly")));
    }

    @Test
    void loginOnlyWithoutRoleOrPerm_passesForAnyLoggedInUser() throws Exception {
        UserContext.set(new LoginUser(3L, "operator", "OUTER_OPER"));
        assertDoesNotThrow(() -> interceptor.preHandle(request, response, handler("loginOnly")));
    }

    @Test
    void nonHandlerMethod_passes() {
        assertDoesNotThrow(() -> interceptor.preHandle(request, response, new Object()));
    }

    private HandlerMethod handler(String method) throws NoSuchMethodException {
        DummyController bean = new DummyController();
        return new HandlerMethod(bean, DummyController.class.getMethod(method));
    }

    /** 测试用伪控制器：覆盖 value/role/perm 三类注解形态。 */
    static class DummyController {

        public void open() {
        }

        @RequireAuth
        public void loginOnly() {
        }

        @RequireAuth(role = "ADMIN")
        public void adminOnly() {
        }

        @RequireAuth(perm = "system:user:create")
        public void permOnly() {
        }
    }
}
