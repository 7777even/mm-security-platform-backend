package com.sinopec.mmsecurity.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * B3 统一包络字段校验：code=0 成功、非 0 业务错误、traceId 必填。
 */
class ResultEnvelopeTest {

    @Test
    void ok_returnsCodeZeroAndData() {
        Result<String> r = Result.ok("payload");
        assertEquals(0, r.getCode());
        assertEquals("ok", r.getMessage());
        assertEquals("payload", r.getData());
        assertNotNull(r.getTraceId());
    }

    @Test
    void ok_withNullData_stillZero() {
        Result<Void> r = Result.ok();
        assertEquals(0, r.getCode());
        assertNull(r.getData());
    }

    @Test
    void fail_returnsGivenCodeAndMessage() {
        Result<Void> r = Result.fail(401, "bad credential");
        assertEquals(401, r.getCode());
        assertEquals("bad credential", r.getMessage());
        assertNotNull(r.getTraceId());
    }

    @Test
    void traceId_fallsBackWhenNoContext() {
        // TraceContext.get() 在无上下文时返回 "j-none"，不得抛 NPE
        Result<Void> r = Result.ok();
        assertEquals("j-none", r.getTraceId());
    }
}
