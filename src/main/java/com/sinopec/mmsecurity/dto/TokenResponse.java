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
}
