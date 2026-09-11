package com.sinopec.mmsecurity.integration;

import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.TokenVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端联调测试（@SpringBootTest + MockMvc，dev profile：H2 + Flyway + 完整过滤器链）。
 *
 * <p>覆盖真实链路：登录 → 受保护端点（menus/devices/dashboard/alarms）200 →
 * 缺 token 401 且带 CORS 头 → 越权（viewer 令牌建警 403）→ 管理员建警 200 →
 * 现场回传身份冒用（非管理员伪报他人 reporter 403）/ 本人或空 reporter 204。</p>
 *
 * <p>与 {@link IntegrationContractTest}（standalone MockMvc，仅串过滤器）互补：
 * 本测试启动真实 Spring 上下文，验证控制器/服务/数据库/鉴权全链路。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class EndToEndFlowTest {

    private static final String ORIGIN = "http://localhost:5173";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 签发测试令牌必须带上用户当前的令牌版本号：本类含登出用例，登出会递增 admin 的版本号，
     * 若仍用不带版本的旧方式签发，令牌会因版本落后被 JwtFilter 判为失效（401）。
     * 按真实登录语义签发（带当前版本）即可，也与生产行为一致。
     */
    @Autowired
    private TokenVersionService tokenVersionService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_DEVICE_CODE = "FAC2026FIREA00000001";

    @Test
    void realLogin_returnsToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin@2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    /** 登录响应不得携带 refreshToken（防 XSS 窃取），refresh 须经 HttpOnly Cookie 下发 */
    @Test
    void login_responseExcludesRefreshToken_butSetsHttpOnlyCookie() throws Exception {
        MockHttpServletResponse resp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin@2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andReturn().getResponse();
        String setCookie = resp.getHeader("Set-Cookie");
        assertNotNull(setCookie, "登录必须下发 Set-Cookie");
        assertTrue(setCookie.contains("rt="), "refresh Cookie 名应为 rt");
        assertTrue(setCookie.contains("HttpOnly"), "refresh Cookie 必须 HttpOnly");
        assertTrue(setCookie.contains("SameSite=Lax"), "refresh Cookie 必须 SameSite=Lax");
    }

    /** 凭 HttpOnly Cookie 续期（无请求体），返回新 access 且仍不暴露 refreshToken */
    @Test
    void refresh_viaCookie_returnsNewAccessToken() throws Exception {
        Cookie rt = loginAndGetRefreshCookie();
        assertNotNull(rt, "登录应下发挥刷新 Cookie");
        mockMvc.perform(post("/api/v1/auth/refresh").cookie(rt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    /** 登出清除 HttpOnly 刷新 Cookie */
    @Test
    void logout_clearsRefreshCookie() throws Exception {
        Cookie rt = loginAndGetRefreshCookie();
        mockMvc.perform(post("/api/v1/auth/logout").cookie(rt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private Cookie loginAndGetRefreshCookie() throws Exception {
        MockHttpServletResponse resp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin@2026\"}"))
                .andReturn().getResponse();
        return resp.getCookie("rt");
    }

    @Test
    void protectedEndpoints_withAdminToken_200() throws Exception {
        String token = jwtUtil.issueAccess("admin", "ADMIN", tokenVersionService.current("admin"));
        String auth = "Bearer " + token;
        mockMvc.perform(get("/api/v1/auth/menus").header("Authorization", auth).header("Origin", ORIGIN))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/devices").header("Authorization", auth).header("Origin", ORIGIN))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/dashboard/overview").header("Authorization", auth).header("Origin", ORIGIN))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/alarms").header("Authorization", auth).header("Origin", ORIGIN))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_noToken_401WithCorsHeader() throws Exception {
        mockMvc.perform(get("/api/v1/alarms").header("Origin", ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    @Test
    void alarmCreate_withViewerToken_403() throws Exception {
        String viewer = jwtUtil.issueAccess("viewer", "VIEWER", tokenVersionService.current("viewer"));
        String body = alarmPayload();
        mockMvc.perform(post("/api/v1/alarms").contentType(JSON).content(body)
                        .header("Authorization", "Bearer " + viewer).header("Origin", ORIGIN))
                .andExpect(status().isForbidden());
    }

    @Test
    void alarmCreate_withAdminToken_200() throws Exception {
        String admin = jwtUtil.issueAccess("admin", "ADMIN", tokenVersionService.current("admin"));
        String body = alarmPayload();
        mockMvc.perform(post("/api/v1/alarms").contentType(JSON).content(body)
                        .header("Authorization", "Bearer " + admin).header("Origin", ORIGIN))
                .andExpect(status().isOk());
    }

    @Test
    void fieldReport_withSpoofedReporter_nonAdmin_403() throws Exception {
        // 水平越权：非管理员伪报他人 reporter → 403
        String viewer = jwtUtil.issueAccess("viewer", "VIEWER", tokenVersionService.current("viewer"));
        String body = "{\"id\":\"u1\",\"kind\":\"field-report\",\"title\":\"t\",\"status\":\"pending\",\"reporter\":\"admin\"}";
        mockMvc.perform(post("/api/v1/field-reports").contentType(JSON).content(body)
                        .header("Authorization", "Bearer " + viewer).header("Origin", ORIGIN))
                .andExpect(status().isForbidden());
    }

    @Test
    void fieldReport_selfOrNull_nonAdmin_204() throws Exception {
        // 本人或空 reporter：服务端绑定为当前登录用户，204
        String viewer = jwtUtil.issueAccess("viewer", "VIEWER", tokenVersionService.current("viewer"));
        String body = "{\"id\":\"u2\",\"kind\":\"field-report\",\"title\":\"t\",\"status\":\"pending\"}";
        mockMvc.perform(post("/api/v1/field-reports").contentType(JSON).content(body)
                        .header("Authorization", "Bearer " + viewer).header("Origin", ORIGIN))
                .andExpect(status().isNoContent());
    }

    private String alarmPayload() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "level", 1,
                "type", "FIRE",
                "deviceCode", VALID_DEVICE_CODE,
                "location", "罐区A",
                "description", "e2e 联调建警"));
    }
}
