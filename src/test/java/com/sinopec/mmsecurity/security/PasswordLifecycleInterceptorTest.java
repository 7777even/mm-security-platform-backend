package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.PasswordStateCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PasswordLifecycleInterceptor：强制首登改密的服务端兜底。
 *
 * <p>dev profile 关闭了默认管理员的种子标记（见 application-dev.yml），故该拦截行为在此以单测固化，
 * 不以集成测试依赖。</p>
 */
class PasswordLifecycleInterceptorTest {

    private final PasswordStateCache cache = mock(PasswordStateCache.class);
    private final PasswordLifecycleInterceptor interceptor = new PasswordLifecycleInterceptor(cache);
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void mutatingRequest_flaggedUser_rejected403() {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        when(cache.mustChange("admin")).thenReturn(true);

        MockHttpServletRequest req = request("POST", "/api/v1/alarms");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(req, response, new Object()));
        assertEquals(ResultCode.FORBIDDEN, ex.getCode());
    }

    @Test
    void readRequest_flaggedUser_passes() {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        when(cache.mustChange("admin")).thenReturn(true);

        assertDoesNotThrow(() -> interceptor.preHandle(request("GET", "/api/v1/alarms"), response, new Object()));
    }

    @Test
    void authEndpoints_flaggedUser_pass_soPasswordCanBeChanged() {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        when(cache.mustChange("admin")).thenReturn(true);

        assertDoesNotThrow(() -> interceptor.preHandle(request("POST", "/api/v1/auth/password"), response, new Object()));
        assertDoesNotThrow(() -> interceptor.preHandle(request("PUT", "/api/v1/auth/profile"), response, new Object()));
        assertDoesNotThrow(() -> interceptor.preHandle(request("POST", "/api/v1/auth/refresh"), response, new Object()));
    }

    @Test
    void auditUplink_flaggedUser_passes() {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        when(cache.mustChange("admin")).thenReturn(true);

        assertDoesNotThrow(() -> interceptor.preHandle(request("POST", "/api/v1/uplink/audit"), response, new Object()));
    }

    @Test
    void unflaggedUser_passes() {
        UserContext.set(new LoginUser(2L, "zhang.san", "OUTER_OPER"));
        when(cache.mustChange("zhang.san")).thenReturn(false);

        assertDoesNotThrow(() -> interceptor.preHandle(request("POST", "/api/v1/alarms"), response, new Object()));
    }

    @Test
    void anonymousRequest_notHandledHere() {
        assertDoesNotThrow(() -> interceptor.preHandle(request("POST", "/api/v1/alarms"), response, new Object()));
    }

    private static MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod(method);
        req.setRequestURI(uri);
        return req;
    }
}
