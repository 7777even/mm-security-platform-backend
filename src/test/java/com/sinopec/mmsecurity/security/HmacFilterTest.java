package com.sinopec.mmsecurity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.common.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HmacFilter 纯单元测试（13.5% → 全覆盖分支）。
 * 仿 JwtFilterTest 用 Spring MockWeb 不起上下文，逐条覆盖：缺头 / 时间戳格式错 / 偏差超限 /
 * 签名不匹配 / 签名匹配放行 / 禁用跳过 / OPTIONS 预检放行。
 */
class HmacFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String secret = "test-secret";

    private HmacFilter filter(boolean enabled, long skewSeconds) {
        return new HmacFilter(enabled, secret, skewSeconds, objectMapper);
    }

    private String sign(String ts, String nonce, String method, String uri) throws Exception {
        String payload = ts + "\n" + nonce + "\n" + method + "\n" + uri;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private MockHttpServletRequest reqWithValidSig(String method, String uri) throws Exception {
        long ts = System.currentTimeMillis();
        String nonce = "nonce-123";
        MockHttpServletRequest req = new MockHttpServletRequest(method, uri);
        req.addHeader("X-Timestamp", String.valueOf(ts));
        req.addHeader("X-Nonce", nonce);
        req.addHeader("X-Signature", sign(String.valueOf(ts), nonce, method, uri));
        return req;
    }

    @Test
    void missingSignatureHeader_rejects401() throws Exception {
        HmacFilter f = filter(true, 5);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/audit/log");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, res, chain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), res.getStatus());
        assertTrue(res.getContentAsString().contains("\"code\":" + ResultCode.SIGNATURE_INVALID));
        assertNull(chain.getRequest(), "缺签名头应短路，不进入后续链");
    }

    @Test
    void badTimestampFormat_rejects401() throws Exception {
        HmacFilter f = filter(true, 5);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/audit/log");
        req.addHeader("X-Timestamp", "not-a-number");
        req.addHeader("X-Nonce", "n");
        req.addHeader("X-Signature", "x");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, res, chain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), res.getStatus());
        assertTrue(res.getContentAsString().contains("时间戳格式错误"));
        assertNull(chain.getRequest());
    }

    @Test
    void timestampSkewExceeded_rejects401Expired() throws Exception {
        HmacFilter f = filter(true, 5);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/audit/log");
        req.addHeader("X-Timestamp", "1");
        req.addHeader("X-Nonce", "n");
        req.addHeader("X-Signature", "x");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, res, chain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), res.getStatus());
        assertTrue(res.getContentAsString().contains("\"code\":" + ResultCode.SIGNATURE_EXPIRED));
        assertNull(chain.getRequest());
    }

    @Test
    void signatureMismatch_rejects401() throws Exception {
        HmacFilter f = filter(true, 5);
        long ts = System.currentTimeMillis();
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/audit/log");
        req.addHeader("X-Timestamp", String.valueOf(ts));
        req.addHeader("X-Nonce", "n");
        req.addHeader("X-Signature", "wrongsignature==");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, res, chain);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), res.getStatus());
        assertTrue(res.getContentAsString().contains("\"code\":" + ResultCode.SIGNATURE_INVALID));
        assertNull(chain.getRequest());
    }

    @Test
    void validSignature_continuesChain() throws Exception {
        HmacFilter f = filter(true, 5);
        MockHttpServletRequest req = reqWithValidSig("POST", "/api/v1/audit/log");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, res, chain);
        assertEquals(HttpStatus.OK.value(), res.getStatus());
        assertTrue(chain.getRequest() != null, "签名正确应放行");
    }

    @Test
    void disabled_skipsVerification() throws Exception {
        HmacFilter f = filter(false, 5);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/audit/log");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, res, chain);
        assertEquals(HttpStatus.OK.value(), res.getStatus());
        assertTrue(chain.getRequest() != null);
    }

    @Test
    void optionsMethod_bypassesSignature() throws Exception {
        HmacFilter f = filter(true, 5);
        MockHttpServletRequest req = new MockHttpServletRequest("OPTIONS", "/api/v1/audit/log");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, res, chain);
        assertEquals(HttpStatus.OK.value(), res.getStatus());
        assertTrue(chain.getRequest() != null);
    }
}
