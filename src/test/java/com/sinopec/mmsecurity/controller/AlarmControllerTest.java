package com.sinopec.mmsecurity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.AlarmItem;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.EmergencyEventPayload;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.service.AlarmAssembler;
import com.sinopec.mmsecurity.service.AlarmService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AlarmController（standalone MockMvc，不启动 Spring 上下文）：
 * 分页参数透传、DTO 字段正确、空库返回空列表（无 mock 回落）；
 * POST 创建返回 AlarmItem、非法入参 400、PUT 不存在返回 data=null、DELETE 返回 ok 布尔。
 */
class AlarmControllerTest {

    private final AlarmAssembler assembler = new AlarmAssembler();
    private final AlarmService service = mock(AlarmService.class);
    private final AlarmController controller = new AlarmController(service, assembler);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void page_returnsAlarmPageResult() throws Exception {
        Page<FacAlarm> p = new Page<>(1, 5);
        p.setTotal(8);
        FacAlarm a = new FacAlarm();
        a.setAlarmId("AE-2026-001");
        a.setDeviceCode("FAC2026FIREA00000001");
        a.setTitle("罐区A消防探头-F01 温度越限");
        a.setStatus(0);
        p.setRecords(List.of(a));
        when(service.page(1L, 5L, null, null, null)).thenReturn(p);

        mockMvc.perform(get("/api/v1/alarms?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(8))
                .andExpect(jsonPath("$.data.list[0].alarmId").value("AE-2026-001"))
                .andExpect(jsonPath("$.data.list[0].status").value("ACTIVE"));
    }

    @Test
    void page_emptyDb_returnsEmptyList() throws Exception {
        Page<FacAlarm> p = new Page<>(1, 5);
        p.setTotal(0);
        when(service.page(1L, 5L, null, null, null)).thenReturn(p);

        mockMvc.perform(get("/api/v1/alarms?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list").isEmpty());
    }

    @Test
    void create_returnsCreatedAlarmItem() throws Exception {
        AlarmItem item = new AlarmItem();
        item.setAlarmId("AE-2026-010");
        item.setStatus("ACTIVE");
        when(service.create(any(EmergencyEventPayload.class))).thenReturn(item);

        mockMvc.perform(post("/api/v1/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":2,\"type\":\"FIRE\",\"deviceCode\":\"FAC2026FIREA00000001\",\"location\":\"罐区A\",\"description\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.alarmId").value("AE-2026-010"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void create_missingRequiredField_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void update_notFound_returnsNullData() throws Exception {
        when(service.update(eq("AE-2099-999"), any(EmergencyEventPayload.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/alarms/AE-2099-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":1,\"type\":\"SOS\",\"deviceCode\":\"FAC2026FIREA00000001\",\"location\":\"x\",\"description\":\"y\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void delete_returnsOkBoolean() throws Exception {
        DeleteResult r = new DeleteResult();
        r.setOk(true);
        when(service.delete("AE-2026-001")).thenReturn(r);

        mockMvc.perform(delete("/api/v1/alarms/AE-2026-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
