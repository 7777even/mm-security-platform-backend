package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 硬控拦截器：零下行控制红线兜底。
 * - POST/PUT/DELETE 命中硬控前缀 → 抛 HARD_CONTROL_BLOCKED
 * - GET 命中硬控前缀 → 放行（只读查询不属下行控制）
 * - 非硬控路径写请求 → 放行
 */
class HardControlInterceptorTest {

    private final HardControlInterceptor interceptor = new HardControlInterceptor();

    @Test
    void hardControlPost_isBlocked() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn("POST");
        when(req.getRequestURI()).thenReturn("/api/v1/devices/cmd");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(req, null, null));
        assertEquals(ResultCode.HARD_CONTROL_BLOCKED, ex.getCode());
    }

    @Test
    void getOnHardControlPath_isAllowed() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn("GET");
        when(req.getRequestURI()).thenReturn("/api/v1/devices/cmd");

        assertTrue(interceptor.preHandle(req, null, null));
    }

    @Test
    void postOnNormalPath_isAllowed() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn("POST");
        when(req.getRequestURI()).thenReturn("/api/v1/devices");

        assertTrue(interceptor.preHandle(req, null, null));
    }
}
