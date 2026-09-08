package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 防重放 HMAC-SHA256 签名过滤器。
 *
 * 生产环境（gateway-bypass=false）强制校验三个请求头：
 *   X-Timestamp : 毫秒时间戳
 *   X-Nonce     : 一次性随机数
 *   X-Signature : HMAC-SHA256(key, timestamp + "\n" + nonce + "\n" + method + "\n" + uri + "\n" + body) 的 Base64
 *
 * Dev 环境（signature.enabled=false）挂起校验，便于联调。
 *
 * 校验规则：
 * 1. 三个头必须齐全
 * 2. 时间戳与服务器时间偏差超过 maxSkewSeconds 拒绝（防重放）
 * 3. HMAC 签名匹配
 *
 * 签名校验失败【直接写出 HTTP 401/403 + B3 包络】，不再抛异常冒泡成 500（同 JwtFilter）。
 *
 * 注册：本类不再使用 @Component 自动注册（顺序不可控），改由
 * {@code SecurityBeans#hmacFilterRegistration} 通过 FilterRegistrationBean 显式装配，
 * 顺序固定为 HIGHEST_PRECEDENCE + 1（在 CorsFilter 之后、JwtFilter 之前）。
 */
@Slf4j
public class HmacFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final String secret;
    private final long maxSkewSeconds;
    private final ObjectMapper objectMapper;

    public HmacFilter(boolean enabled, String secret, long maxSkewSeconds, ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.secret = secret;
        this.maxSkewSeconds = maxSkewSeconds;
        this.objectMapper = objectMapper;
    }

    private static final String H = "HmacSHA256";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 探针路径免签名：K8s liveness/readiness 探针无法携带 HMAC 头，必须放行
        // OPTIONS 预检亦放行：与 JwtFilter 同理，避免预检在签名校验阶段被拒导致缺 CORS 头
        return !enabled || uri.startsWith("/h2-console") || uri.startsWith("/actuator")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ts = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        String sign = request.getHeader("X-Signature");

        if (ts == null || nonce == null || sign == null) {
            reject(ResultCode.SIGNATURE_INVALID, "缺少签名头(X-Timestamp/X-Nonce/X-Signature)", response);
            return;
        }

        long clientTs;
        try {
            clientTs = Long.parseLong(ts);
        } catch (NumberFormatException e) {
            reject(ResultCode.SIGNATURE_INVALID, "时间戳格式错误", response);
            return;
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - clientTs) > maxSkewSeconds * 1000) {
            reject(ResultCode.SIGNATURE_EXPIRED, "请求已过期（时间偏差超限）", response);
            return;
        }

        String payload = ts + "\n" + nonce + "\n" + request.getMethod() + "\n" + request.getRequestURI();
        String expected = hmacSha256Base64(secret, payload);
        if (!MessageDigest.isEqual(sign.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))) {
            reject(ResultCode.SIGNATURE_INVALID, "签名不匹配", response);
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 签名校验失败：直接写出 HTTP 401/403 + B3 包络（UTF-8 JSON），不抛异常。
     */
    private void reject(int code, String msg, HttpServletResponse response) throws IOException {
        HttpStatus status = (code == ResultCode.UNAUTHORIZED) ? HttpStatus.UNAUTHORIZED
                : (code == ResultCode.FORBIDDEN) ? HttpStatus.FORBIDDEN
                : HttpStatus.UNAUTHORIZED;
        log.warn("[{}] HMAC 签名校验失败 code={} msg={}", TraceContext.get(), code, msg);
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, msg)));
        response.getWriter().flush();
    }

    private String hmacSha256Base64(String key, String data) {
        try {
            Mac mac = Mac.getInstance(H);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), H));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }
}
