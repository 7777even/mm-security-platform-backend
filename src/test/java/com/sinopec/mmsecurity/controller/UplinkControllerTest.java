package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.service.UplinkService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UplinkController（standalone MockMvc，不启动 Spring 上下文）：
 * /audit/log 走 B3 包络；/field-reports 返回 204（非 B3，契约明示 bypass）。
 */
class UplinkControllerTest {

    private final UplinkService service = mock(UplinkService.class);
    private final UplinkController controller = new UplinkController(service);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void reportAudit_returnsB3Ok() throws Exception {
        mockMvc.perform(post("/api/v1/audit/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"events\":[{\"action\":\"route.view\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        verify(service).reportAudit(any());
    }

    @Test
    void reportAudit_emptyEvents_returns100() throws Exception {
        mockMvc.perform(post("/api/v1/audit/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"events\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void fieldReports_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/field-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"r1\",\"kind\":\"field-report\",\"title\":\"A2 区火情处置\",\"status\":\"done\",\"attempts\":1,\"createdAt\":1717488000000}"))
                .andExpect(status().isNoContent());
        verify(service).submitFieldReport(any());
    }
}
