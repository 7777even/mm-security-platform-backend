package com.sinopec.mmsecurity.common;

import com.sinopec.mmsecurity.controller.AlarmController;
import com.sinopec.mmsecurity.service.AlarmAssembler;
import com.sinopec.mmsecurity.service.AlarmService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GlobalExceptionHandler 的 Web 层异常映射守门测试（standalone MockMvc，不启动 Spring 上下文）。
 *
 * <p><b>回归目标（2026-09-09「数据端点全 500」阻塞复盘）：</b>
 * 请求体类型/结构偏差、查询参数类型错、方法/媒体类型不支持、DB 约束冲突，
 * 这些本质是「调用方入参问题」的场景，必须返回 400/405/415/409 语义与可自查的 message，
 * <b>绝不允许回退为 500「服务器内部错误」</b>——否则联调期字段偏差会全部伪装成服务端故障。
 *
 * <p>用 AlarmController 作为宿主：其 EmergencyEventPayload 含 Integer level 字段，
 * 是复现「契约要求整数、前端传字符串枚举」的天然样本。
 */
class GlobalExceptionHandlerTest {

    private final AlarmService service = mock(AlarmService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AlarmController(service, new AlarmAssembler()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    /** level 契约为 Integer，前端传字符串 "HIGH" —— 曾直接 500，现应为 code=100 且点名字段与期望类型。 */
    @Test
    void bodyFieldTypeMismatch_returnsParamInvalidNamingField() throws Exception {
        String body = "{\"level\":\"HIGH\",\"type\":\"FIRE\",\"deviceCode\":\"FAC2026FIREA00000001\","
                + "\"location\":\"罐区A\",\"description\":\"温度越限\"}";

        mockMvc.perform(post("/api/v1/alarms").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("level")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("整数")));
    }

    /** 单对象接口误传数组体（批量接口最常踩）—— 曾直接 500，现应为 code=100 且提示期望 JSON 对象。 */
    @Test
    void bodyStructureMismatch_returnsParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"level\":1,\"type\":\"FIRE\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("JSON 对象")));
    }

    /** JSON 语法错误（缺右括号）应收敛为参数错误而非 500。 */
    @Test
    void malformedJson_returnsParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":1,"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID));
    }

    /** 查询参数类型不匹配（?page=abc）应为 code=100 并点名参数。 */
    @Test
    void queryParamTypeMismatch_returnsParamInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/alarms?page=abc&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("page")));
    }

    /** 用错 HTTP 动词应为 405，而非 500。 */
    @Test
    void unsupportedMethod_returns405() throws Exception {
        mockMvc.perform(patch("/api/v1/alarms"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID));
    }

    /** Content-Type 非 JSON 应为 415，而非 500。 */
    @Test
    void unsupportedMediaType_returns415() throws Exception {
        mockMvc.perform(post("/api/v1/alarms")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("level=1"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID));
    }

    /** DB 约束冲突应映射为 409 且不外泄表名/约束名。 */
    @Test
    void dataIntegrityViolation_returns409WithoutLeakingSchema() throws Exception {
        when(service.create(any())).thenThrow(
                new DataIntegrityViolationException("Unique index or primary key violation: PK_FAC_ALARM ON FAC_ALARM(ID)"));
        String body = "{\"level\":1,\"type\":\"FIRE\",\"deviceCode\":\"FAC2026FIREA00000001\","
                + "\"location\":\"罐区A\",\"description\":\"温度越限\"}";

        mockMvc.perform(post("/api/v1/alarms").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ResultCode.CONFLICT))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("FAC_ALARM"))));
    }

    /** @Valid 必填校验仍走原有映射（防止本次扩展把校验路径打偏）。 */
    @Test
    void missingRequiredField_stillReturnsValidationMessage() throws Exception {
        mockMvc.perform(post("/api/v1/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":1,\"type\":\"FIRE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("必填")));
    }
}
