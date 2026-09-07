package com.sinopec.mmsecurity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DeviceController（standalone MockMvc，不启动 Spring 上下文）：
 * 分页参数透传、DTO 字段正确、空库返回空列表。
 */
class DeviceControllerTest {

    private final DeviceService service = mock(DeviceService.class);
    private final DeviceController controller = new DeviceController(service);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void page_returnsDevicePageResult() throws Exception {
        Page<FacDevice> p = new Page<>(1, 5);
        p.setTotal(8);
        FacDevice d = new FacDevice();
        d.setDeviceCode("FAC2026FIREA00000001");
        d.setDeviceName("罐区A消防探头-F01");
        p.setRecords(List.of(d));
        when(service.page(1L, 5L, null, null, null)).thenReturn(p);

        mockMvc.perform(get("/api/v1/devices?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(8))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.list[0].deviceCode").value("FAC2026FIREA00000001"));
    }

    @Test
    void page_emptyDb_returnsEmptyList() throws Exception {
        Page<FacDevice> p = new Page<>(1, 5);
        p.setTotal(0);
        when(service.page(1L, 5L, null, null, null)).thenReturn(p);

        mockMvc.perform(get("/api/v1/devices?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list").isEmpty());
    }
}
