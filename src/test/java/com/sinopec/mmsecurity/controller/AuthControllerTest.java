package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.LoginRequest;
import com.sinopec.mmsecurity.dto.TokenResponse;
import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.service.AccountService;
import com.sinopec.mmsecurity.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController（standalone MockMvc，不启动 Spring 上下文）：
 * 登录/刷新成功返回 access token 且 refresh 仅经 HttpOnly Cookie 下发（body 绝不出现 refreshToken）；
 * 口令错误返回业务错误码（由 GlobalExceptionHandler 收敛为 B3 包络）。
 */
class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AccountService accountService = mock(AccountService.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final AuthController controller = new AuthController(authService, accountService, jwtUtil);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void login_success_returnsToken_and_setsRefreshCookie() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(TokenResponse.of("at", 7200));
        when(jwtUtil.issueRefresh("admin")).thenReturn("rt-cookie");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin@2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").value("at"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(header().string("Set-Cookie", containsString("rt=")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")));
    }

    @Test
    void login_wrongPassword_returnsBusinessError() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException(401, "用户名或密码错误"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"WRONG\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void refresh_success_returnsToken_and_rotatesRefreshCookie() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(jwtUtil.parse("rt")).thenReturn(claims);
        when(authService.refresh("rt")).thenReturn(TokenResponse.of("at", 7200));
        when(jwtUtil.issueRefresh("admin")).thenReturn("rt-cookie");

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("rt", "rt")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").value("at"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(header().string("Set-Cookie", containsString("rt=")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")));
    }

    @Test
    void refresh_missingCookie_returnsTokenInvalid() throws Exception {
        // 无 refresh Cookie → Controller 抛 TOKEN_INVALID（B3 code=202，HTTP 200）
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.TOKEN_INVALID));
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        when(authService.refresh("bad")).thenThrow(new BusinessException(401, "刷新令牌无效"));

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("rt", "bad")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
