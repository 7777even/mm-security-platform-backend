package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.LoginRequest;
import com.sinopec.mmsecurity.dto.MenuVO;
import com.sinopec.mmsecurity.dto.TokenResponse;
import com.sinopec.mmsecurity.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 认证域：登录、续期、登出、当前用户、菜单。
 * 这些路径免鉴权（已在 JwtFilter 白名单），前端脚手架 login/refresh/logout/me/menus 对接。
 *
 * <p>刷新令牌安全策略（S1 §5.3 合规红线）：refresh 令牌<b>绝不</b>进入响应 body，
 * 仅由后端经 {@code Set-Cookie} 下发 {@code HttpOnly} Cookie（name=rt，SameSite=Lax）。
 * 前端 JS 不可读，规避 XSS 窃刷新令牌；access 令牌仍走前端内存态（token.ts）。
 * 续期时浏览器自动携带该 Cookie，后端从 {@code @CookieValue} 读取，
 * 因此 {@code POST /auth/refresh} 无需请求体。登出时后端下发 Max-Age=0 的同名 Cookie 清除之。</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE = "rt";

    private final AuthService authService;

    /** 生产 HTTPS 下刷新 Cookie 须 Secure 才生效；dev(http) 必须为 false，否则浏览器拒存。 */
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        TokenResponse t = authService.login(req);
        addRefreshCookie(response, t.getRefreshToken(), t.getExpiresIn());
        // 对外响应只带 access + 有效期，refresh 已通过 Cookie 下发（body 不暴露）
        return Result.ok(TokenResponse.of(t.getAccessToken(), t.getExpiresIn()));
    }

    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "缺少 refresh 令牌（Cookie）");
        }
        TokenResponse t = authService.refresh(refreshToken);
        addRefreshCookie(response, t.getRefreshToken(), t.getExpiresIn());
        return Result.ok(TokenResponse.of(t.getAccessToken(), t.getExpiresIn()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        clearRefreshCookie(response);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.ok(authService.me());
    }

    @GetMapping("/menus")
    public Result<List<MenuVO>> menus() {
        return Result.ok(authService.menus());
    }

    /** 种 HttpOnly 刷新 Cookie：浏览器自动随同站请求回传，前端 JS 读不到。 */
    private void addRefreshCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        ResponseCookie rc = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, rc.toString());
    }

    /** 清刷新 Cookie：下发同名 Max-Age=0 Cookie。 */
    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie rc = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, rc.toString());
    }
}
