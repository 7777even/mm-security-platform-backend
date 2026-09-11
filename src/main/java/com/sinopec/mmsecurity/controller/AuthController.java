package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.LoginRequest;
import com.sinopec.mmsecurity.dto.MeResult;
import com.sinopec.mmsecurity.dto.MenuVO;
import com.sinopec.mmsecurity.dto.PasswordChangeRequest;
import com.sinopec.mmsecurity.dto.ProfileUpdateRequest;
import com.sinopec.mmsecurity.dto.TokenResponse;
import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.TokenVersionService;
import io.jsonwebtoken.Claims;
import com.sinopec.mmsecurity.service.AccountService;
import com.sinopec.mmsecurity.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证域：登录、续期、登出、当前用户、菜单。
 * 仅 login/refresh/logout 免鉴权（在 JwtFilter 白名单，因拿/换/清令牌本身不能要求令牌）；
 * me/menus 已移出白名单，必须携带有效 access 令牌（Bearer），由 JwtFilter 解析后注入 UserContext，
 * 供 AuthService 取真实身份（me）与按角色过滤菜单（menus，RBAC）。前端脚手架对应对接。
 *
 * <p>刷新令牌安全策略（S1 §5.3 合规红线）：refresh 令牌<b>绝不</b>进入响应 body，
 * 仅由后端经 {@code Set-Cookie} 下发 {@code HttpOnly} Cookie（name=rt，SameSite=Lax）。
 * {@link TokenResponse} 不再持有 refresh 字段，从源头杜绝 body 泄露；
 * 刷新令牌由 {@link AuthService#issueRefreshToken} 签发后在此层种入 Cookie，前端 JS 不可读，
 * 规避 XSS 窃刷新令牌；access 令牌仍走前端内存态（token.ts）。
 * 续期时浏览器自动携带该 Cookie，后端从 {@code @CookieValue} 读取，
 * 因此 {@code POST /auth/refresh} 无需请求体。登出时后端下发 Max-Age=0 的同名 Cookie 清除之。</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE = "rt";

    private final AuthService authService;
    private final AccountService accountService;
    private final JwtUtil jwtUtil;
    private final TokenVersionService tokenVersionService;

    /** 生产 HTTPS 下刷新 Cookie 须 Secure 才生效；dev(http) 必须为 false，否则浏览器拒存。 */
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        TokenResponse t = authService.login(req);
        String rt = authService.issueRefreshToken(req.getUsername());
        addRefreshCookie(response, rt, t.getExpiresIn());
        return Result.ok(t);
    }

    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "缺少 refresh 令牌（Cookie）");
        }
        TokenResponse t = authService.refresh(refreshToken);
        String username = jwtUtil.parse(refreshToken).getSubject();
        String newRt = authService.issueRefreshToken(username);
        addRefreshCookie(response, newRt, t.getExpiresIn());
        return Result.ok(t);
    }

    /**
     * 登出：清客户端刷新 Cookie + 服务端递增令牌版本号。
     *
     * <p><b>修复</b>：此前只清 Cookie，已签发的 access token 在其剩余有效期
     * （jwt.access-ttl，默认 2 小时）内仍可通过校验；refresh 令牌（7 天）被窃取后可无限续期。
     * 递增版本号后，旧令牌在 {@code JwtFilter} 校验时因版本落后被拒。</p>
     */
    @PostMapping("/logout")
    public Result<Void> logout(
            HttpServletRequest request,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        String username = resolveLogoutUsername(request, refreshToken);
        if (username != null && !username.isBlank()) {
            tokenVersionService.bump(username);
        }
        clearRefreshCookie(response);
        return Result.ok();
    }

    /**
     * 尽力解析登出用户名：优先 access 令牌（Bearer 头），退回 refresh Cookie。
     * 两者都解析不到（令牌已过期或未携带）时返回 null——此时无从定位用户，
     * 清 Cookie 已是当时唯一能做的兜底，不因此让登出失败。
     */
    private String resolveLogoutUsername(HttpServletRequest request, String refreshToken) {
        String header = request == null ? null : request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Claims claims = jwtUtil.parse(header.substring(7));
            if (claims != null && "access".equals(claims.get("type", String.class))) {
                return claims.getSubject();
            }
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            Claims claims = jwtUtil.parse(refreshToken);
            if (claims != null) {
                return claims.getSubject();
            }
        }
        return null;
    }

    @GetMapping("/me")
    public Result<MeResult> me() {
        return Result.ok(authService.me());
    }

    @GetMapping("/menus")
    public Result<List<MenuVO>> menus() {
        return Result.ok(authService.menus());
    }

    /** 本人修改口令（须校验旧口令；成功后清除强制改密标记）。 */
    @PostMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest payload) {
        accountService.changePassword(payload);
        return Result.ok();
    }

    /** 本人资料修改（仅姓名；角色/状态不可自改）。返回最新 me。 */
    @PutMapping("/profile")
    public Result<MeResult> updateProfile(@Valid @RequestBody ProfileUpdateRequest payload) {
        return Result.ok(accountService.updateProfile(payload));
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
