package com.sinopec.mmsecurity.common;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 分布式链路追踪根 span 过滤器（OpenTelemetry / Micrometer Tracing 桥接）。
 *
 * <p>位置：{@code SecurityBeans} 中以 {@code HIGHEST_PRECEDENCE} 注册，与 CorsFilter 同优先级、
 * 确定性早于 HmacFilter(+1) / JwtFilter(+10)。这保证【所有】下游过滤器、业务日志与
 * {@code Result.traceId} 在请求进入业务前就拿到了统一 traceId。
 *
 * <p>为何必须显式建根：Spring Boot 自带的 web 观测过滤器（ServerHttpObservationFilter）位于
 * DispatcherServlet 附近、晚于本过滤器；若只靠它，JwtFilter 处 MDC 尚为空、响应 traceId 与
 * 下游 span 不一致。本过滤器先建根 span 并注入 MDC，Spring 的 web 观测过滤器会基于当前 span
 * 派生子 span，形成单条 trace 树，不会重复建根。
 *
 * <p>行为：
 *  - 若 MDC 已有 traceId（兜底，如上游/网关注入的 traceparent 已建根），直接放行复用，避免分裂成两条 trace；
 *  - 否则用 Tracer 起 root span + 注入 MDC(traceId/spanId) + finally 清理，
 *    使日志 pattern [%X{traceId}] 与 Result.traceId 均为标准 32 位 hex（对齐 OTel，弃用旧 j-xxxx）。
 */
@Slf4j
public class TracingFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public TracingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 兜底：MDC 已有 traceId（上游已建根，或线程池残留）则复用，不另起根，避免产生两条 trace
        if (MDC.get("traceId") != null) {
            chain.doFilter(request, response);
            return;
        }

        io.micrometer.tracing.Span span = tracer.nextSpan()
                .name(request.getMethod() + " " + request.getRequestURI())
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            MDC.put("traceId", span.context().traceId());
            MDC.put("spanId", span.context().spanId());
            chain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
            MDC.remove("spanId");
            span.end();
        }
    }
}
