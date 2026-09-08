package com.sinopec.mmsecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录/刷新对外响应。仅含 access + 有效期。
 * 刷新令牌（refresh）绝不进入本 DTO / 响应 body，仅由后端经 HttpOnly Cookie 下发（见 AuthController）。
 * 前端把 access 存入内存态（token.ts），不碰刷新令牌，规避 XSS 窃刷新令牌。
 */
@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private long expiresIn;
    private String tokenType;

    /**
     * 对外响应构造：仅含 access + 有效期，refresh 由后端经 HttpOnly Cookie 下发（绝不进 body）。
     */
    public static TokenResponse of(String access, long ttl) {
        return new TokenResponse(access, ttl, "Bearer");
    }
}
