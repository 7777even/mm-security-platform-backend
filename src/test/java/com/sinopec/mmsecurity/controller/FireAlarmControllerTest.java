package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.FireAlarmCreateRequest;
import com.sinopec.mmsecurity.dto.FireAlarmItem;
import com.sinopec.mmsecurity.dto.FireAlarmPageResult;
import com.sinopec.mmsecurity.service.FireAlarmService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FireAlarmController（standalone MockMvc，不启动 Spring 上下文）：
 * 分页端点包络结构、list/page/size/total 透传、字段透传。
 * 鉴权由 RequireAuthInterceptor 在 WebMvcConfig 注册，standalone 不挂载，故此处不校验登录态。
 */
class FireAlarmControllerTest {

    private final FireAlarmService service = mock(FireAlarmService.class);
    private final FireAlarmController controller = new FireAlarmController(service);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void page_returnsPageResult() throws Exception {
        FireAlarmItem item = new FireAlarmItem();
        item.setAlarmId("FA-20260907-001");
        item.setTypeLabel("火灾报警");
        item.setStatus("ACTIVE");
        item.setTitle("蜡油加氢装置火灾");

        FireAlarmPageResult p = new FireAlarmPageResult();
        p.setList(List.of(item));
        p.setTotal(16);
        p.setPage(1);
        p.setSize(10);
        when(service.page(1L, 10L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/fire-alarms").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(16))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.list[0].alarmId").value("FA-20260907-001"))
                .andExpect(jsonPath("$.data.list[0].status").value("ACTIVE"));
    }

    @Test
    void page_defaultPageSize_usesDefaults() throws Exception {
        FireAlarmPageResult p = new FireAlarmPageResult();
        p.setList(List.of());
        p.setTotal(0);
        p.setPage(1);
        p.setSize(10);
        when(service.page(1L, 10L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/fire-alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list").isEmpty());
    }

    /* ==================== 新增 create ==================== */

    @Test
    void create_returnsItem() throws Exception {
        FireAlarmItem item = new FireAlarmItem();
        item.setAlarmId("FA-20261001-ABC123456789");
        item.setTitle("联动测试报警");
        item.setStatus("ACTIVE");
        when(service.create(any(FireAlarmCreateRequest.class))).thenReturn(item);

        mockMvc.perform(post("/api/v1/fire-alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"联动测试报警\",\"time\":\"2026-10-01 21:00:00\","
                                + "\"typeLabel\":\"火灾报警\",\"typeTone\":\"fire\","
                                + "\"location\":\"化工区-测试\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.alarmId").value("FA-20261001-ABC123456789"));
    }

    /**
     * 缺 title（@NotBlank 触发）→ 走 GlobalExceptionHandler 收敛到 B3 包络。
     * 约定：参数类错误返回 HTTP 200 + code=100（PARAM_INVALID），由前端按 code 判定，并非 400。
     */
    @Test
    void create_missingTitle_returnsParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/fire-alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"time\":\"2026-10-01 21:00:00\"}"))  // 缺 title
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(100))
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    /* ==================== 删除 delete ==================== */

    @Test
    void delete_existing_returnsOk() throws Exception {
        doNothing().when(service).delete("FA-DEL-001");
        mockMvc.perform(delete("/api/v1/fire-alarms/FA-DEL-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
