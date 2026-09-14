package com.sinopec.mmsecurity.common;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TraceContext 纯读取封装校验：读 MDC(traceId)，无上下文回落 j-none。
 * 不再有 init()/clear()（由 TracingFilter 注入 MDC），故只验证 get()。
 */
class TraceContextTest {

    @Test
    void get_returnsMdcTraceId_whenPresent() {
        // 标准 32 位 hex（对齐 OTel，替代旧 j-xxxx）
        MDC.put("traceId", "4bf92f3577b34da6a3ce929d0e0e4736");
        try {
            assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", TraceContext.get());
        } finally {
            MDC.remove("traceId");
        }
    }

    @Test
    void get_returnsJNone_whenAbsent() {
        MDC.remove("traceId");
        assertEquals("j-none", TraceContext.get());
    }
}
