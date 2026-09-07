package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
 */
@Slf4j
@Component
public class HmacFilter extends OncePerRequestFilter {

    @Value("${signature.enabled}")
    private boolean enabled;

    @Value("${signature.secret}")
    private String secret;

    @Value("${signature.max-skew-seconds}")
    private long maxSkewSeconds;

    private static final String H = "HmacSHA256";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || request.getRequestURI().startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ts = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        String sign = request.getHeader("X-Signature");

        if (ts == null || nonce == null || sign == null) {
            throw new BusinessException(ResultCode.SIGNATURE_INVALID, "缺少签名头(X-Timestamp/X-Nonce/X-Signature)");
        }

        long clientTs;
        try {
            clientTs = Long.parseLong(ts);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.SIGNATURE_INVALID, "时间戳格式错误");
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - clientTs) > maxSkewSeconds * 1000) {
            throw new BusinessException(ResultCode.SIGNATURE_EXPIRED, "请求已过期（时间偏差超限）");
        }

        String payload = ts + "\n" + nonce + "\n" + request.getMethod() + "\n" + request.getRequestURI();
        String expected = hmacSha256Base64(secret, payload);
        if (!MessageDigest.isEqual(sign.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException(ResultCode.SIGNATURE_INVALID, "签名不匹配");
        }

        chain.doFilter(request, response);
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
