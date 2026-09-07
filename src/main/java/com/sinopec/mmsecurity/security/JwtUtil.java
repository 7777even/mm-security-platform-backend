package com.sinopec.mmsecurity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具：签发与校验。
 * 令牌走服务端签发，前端脚手架要求内存态（禁止 localStorage 明文），
 * 由前端 HttpOnly Cookie / access-token 头承载，后端不存储令牌状态（无状态）。
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-ttl}")
    private long accessTtl;

    @Value("${jwt.refresh-ttl}")
    private long refreshTtl;

    @Value("${jwt.issuer}")
    private String issuer;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发 access token（短效 2h） */
    public String issueAccess(String username, String role) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claim("role", role)
                .claim("type", "access")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTtl * 1000))
                .signWith(key())
                .compact();
    }

    /** 签发 refresh token（长效 7d） */
    public String issueRefresh(String username) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claim("type", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTtl * 1000))
                .signWith(key())
                .compact();
    }

    /** 校验并解包 claims，失败返回 null */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            log.debug("JWT parse failed: {}", e.getMessage());
            return null;
        }
    }
}
