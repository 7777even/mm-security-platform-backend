package com.sinopec.mmsecurity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.TraceContext;
import com.sinopec.mmsecurity.config.RateLimitProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口级限流防刷过滤器（进程内令牌桶，零外部依赖）。
 *
 * 装配顺序（见 SecurityBeans）：HmacFilter(HIGHEST+1) 之后、JwtFilter(HIGHEST+10) 之前（order = +2）。
 * 含义：请求先过 HMAC 签名校验（无有效签名的裸请求已在 Hmac 层被 401 挡掉，不消耗限流配额），
 * 通过签名的合法流量再限速，节省鉴权开销、也避免给恶意裸请求做限流计数。
 *
 * 放行规则（shouldNotFilter，与 HmacFilter 对齐）：
 *   - /actuator 探针路径（K8s liveness/readiness 不能受限流误杀）
 *   - /h2-console（仅 dev）
 *   - OPTIONS 预检（保证 CORS 头不被挡）
 *
 * 限流维度：key = 客户端IP（命中白名单直接放行）。命中 path 规则时用该规则的 limiter，
 * 否则用全局 limiter；不同规则各自独立计数。
 *
 * 可观测性：被限流的请求通过 Micrometer Counter {@code rate_limit_exceeded_total} 暴露，
 * 供 Prometheus 抓取、Grafana 绘制（维度刻意不加 ip/uri 标签，避免高基数爆炸）。
 *
 * 被限流：返回 HTTP 429 + B3 包络 JSON + Retry-After 头（与 Hmac/Jwt 拒绝写法一致，不抛异常冒泡成 500）。
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final ObjectMapper objectMapper;
    private final Counter rateLimitedTotal;
    private final AntPathMatcher matcher = new AntPathMatcher();

    /** 规则 → 限流器 映射；全局规则 key 为 "__global__" */
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    /** 各规则的 key 后缀（用于区分不同规则的桶） */
    private final ConcurrentHashMap<String, String> ruleKeys = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties props, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.rateLimitedTotal = (meterRegistry == null) ? null
                : Counter.builder("rate_limit_exceeded_total")
                    .description("被接口限流过滤器拒绝的请求数")
                    .register(meterRegistry);
        // 预建全局限流器
        limiters.put("__global__", new RateLimiter(props.getGlobalQps(), props.getGlobalBurst()));
        for (RateLimitProperties.PathRule rule : props.getRules()) {
            String rk = "rule:" + rule.getPath();
            limiters.put(rk, new RateLimiter(rule.getQps(), rule.getBurst()));
            ruleKeys.put(rule.getPath(), rk);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator") || uri.startsWith("/h2-console")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!props.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        if (props.getIpWhitelist().contains(clientIp)) {
            chain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        String limiterKey = "__global__";
        String rulePath = null;
        for (RateLimitProperties.PathRule rule : props.getRules()) {
            if (matcher.match(rule.getPath(), uri)) {
                limiterKey = ruleKeys.get(rule.getPath());
                rulePath = rule.getPath();
                break;
            }
        }

        RateLimiter limiter = limiters.get(limiterKey);
        // key = IP + 规则维度，避免单 IP 多路径互相耗尽；全局规则下 path 维度退化为 IP
        String key = rulePath == null ? clientIp : (clientIp + ":" + rulePath);

        if (limiter.tryAcquire(key)) {
            chain.doFilter(request, response);
        } else {
            reject(clientIp, uri, response);
        }
    }

    private void reject(String ip, String uri, HttpServletResponse response) throws IOException {
        log.warn("[{}] 接口限流触发 ip={} uri={}", TraceContext.get(), ip, uri);
        if (rateLimitedTotal != null) {
            rateLimitedTotal.increment();
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
        response.setHeader("Retry-After", "1");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.fail(429, "请求过于频繁，请稍后重试")));
        response.getWriter().flush();
    }

    /**
     * 解析客户端真实 IP：trust-forwarded 时优先取 X-Forwarded-For 第一个地址，
     * 否则回退 request.getRemoteAddr()。
     */
    private String resolveClientIp(HttpServletRequest request) {
        if (props.isTrustForwarded()) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
