package com.sinopec.mmsecurity.common;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 请求 traceId 容器，用于 Result.traceId 与日志链路追踪。
 */
public final class TraceContext {

    public static final String KEY = "traceId";

    private TraceContext() {}

    public static void init() {
        String trace = UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        MDC.put(KEY, "j" + trace);
    }

    public static String get() {
        String v = MDC.get(KEY);
        return v == null ? "j-none" : v;
    }

    public static void clear() {
        MDC.remove(KEY);
    }
}
