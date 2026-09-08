package com.sinopec.mmsecurity.integration;

import com.sinopec.mmsecurity.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

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

    @Test
    void protectedEndpoints_withAdminToken_200() throws Exception {
        String token = jwtUtil.issueAccess("admin", "ADMIN");
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
        String viewer = jwtUtil.issueAccess("viewer", "VIEWER");
        String body = alarmPayload();
        mockMvc.perform(post("/api/v1/alarms").contentType(JSON).content(body)
                        .header("Authorization", "Bearer " + viewer).header("Origin", ORIGIN))
                .andExpect(status().isForbidden());
    }

    @Test
    void alarmCreate_withAdminToken_200() throws Exception {
        String admin = jwtUtil.issueAccess("admin", "ADMIN");
        String body = alarmPayload();
        mockMvc.perform(post("/api/v1/alarms").contentType(JSON).content(body)
                        .header("Authorization", "Bearer " + admin).header("Origin", ORIGIN))
                .andExpect(status().isOk());
    }

    @Test
    void fieldReport_withSpoofedReporter_nonAdmin_403() throws Exception {
        // 水平越权：非管理员伪报他人 reporter → 403
        String viewer = jwtUtil.issueAccess("viewer", "VIEWER");
        String body = "{\"id\":\"u1\",\"kind\":\"field-report\",\"title\":\"t\",\"status\":\"pending\",\"reporter\":\"admin\"}";
        mockMvc.perform(post("/api/v1/field-reports").contentType(JSON).content(body)
                        .header("Authorization", "Bearer " + viewer).header("Origin", ORIGIN))
                .andExpect(status().isForbidden());
    }

    @Test
    void fieldReport_selfOrNull_nonAdmin_204() throws Exception {
        // 本人或空 reporter：服务端绑定为当前登录用户，204
        String viewer = jwtUtil.issueAccess("viewer", "VIEWER");
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
