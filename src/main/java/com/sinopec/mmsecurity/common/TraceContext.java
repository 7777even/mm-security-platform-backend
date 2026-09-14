package com.sinopec.mmsecurity.common;

import org.slf4j.MDC;

/**
 * 请求 traceId 读取封装。
 *
 * <p>traceId 由 {@code TracingFilter}（OpenTelemetry / Micrometer Tracing 桥接）注入
 * MDC(key=traceId，标准 32 位 hex)，此处仅读取，保证日志 pattern [%X{traceId}]
 * 与 {@code Result.traceId} 使用同一值（对齐 OTel，弃用旧 j-xxxx 格式）。
 *
 * <p>无请求上下文（启动期、单元测试、非 Web 线程）回落 {@code "j-none"}，保持兼容。
 */
public final class TraceContext {

    public static final String KEY = "traceId";

    private TraceContext() {}

    public static String get() {
        String v = MDC.get(KEY);
        return v == null ? "j-none" : v;
    }
}
