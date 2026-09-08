package com.sinopec.mmsecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录/刷新统一返回。前端把 access 存入内存/HttpOnly-Cookie，不存 localStorage。
 */
@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;

    public static TokenResponse of(String access, String refresh, long ttl) {
        return new TokenResponse(access, refresh, ttl, "Bearer");
    }

    /**
     * 对外响应构造：仅含 access + 有效期，refresh 由后端经 HttpOnly Cookie 下发（绝不进 body）。
     * refreshToken 置 null，配合应用全局 jackson default-property-inclusion=non_null 不会被序列化，
     * 前端 JS 拿不到 refresh 令牌，杜绝 XSS 窃刷新令牌。
     */
    public static TokenResponse of(String access, long ttl) {
        return new TokenResponse(access, null, ttl, "Bearer");
    }
}
