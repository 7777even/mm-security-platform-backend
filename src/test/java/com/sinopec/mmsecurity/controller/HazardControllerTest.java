package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.FacilityDetailInfo;
import com.sinopec.mmsecurity.dto.MajorHazardDetail;
import com.sinopec.mmsecurity.dto.MajorHazardItem;
import com.sinopec.mmsecurity.dto.MonitoringAlarm;
import com.sinopec.mmsecurity.dto.MonitoringPoint;
import com.sinopec.mmsecurity.service.HazardService;
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
 * HazardController（standalone MockMvc，不启动 Spring 上下文）：
 * 5 个端点的包络结构、字段透传、可选 name 参数、null 数据省略 data 字段（@JsonInclude NON_NULL）。
 * 鉴权由 RequireAuthInterceptor 在 WebMvcConfig 注册，standalone 不挂载，故此处不校验登录态。
 */
class HazardControllerTest {

    private final HazardService service = mock(HazardService.class);
    private final HazardController controller = new HazardController(service);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void listMajorHazards_returnsItems() throws Exception {
        MajorHazardItem item = new MajorHazardItem();
        item.setId(1L);
        item.setName("乙烯球罐区");
        item.setLevel("一级");
        when(service.listMajorHazards()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/hazards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("乙烯球罐区"));
    }

    @Test
    void getMajorHazardDetail_returnsDetail() throws Exception {
        MajorHazardDetail d = new MajorHazardDetail();
        d.setId(1L);
        d.setName("乙烯球罐区");
        d.setKeyProcess(true);
        when(service.getMajorHazardDetail(1L)).thenReturn(d);

        mockMvc.perform(get("/api/v1/hazards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.keyProcess").value(true));
    }

    @Test
    void getMajorHazardDetail_notFound_omitsDataField() throws Exception {
        when(service.getMajorHazardDetail(404L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/hazards/404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void listMonitoringPoints_returnsPoints() throws Exception {
        MonitoringPoint p = new MonitoringPoint();
        p.setId("MP-01");
        p.setName("罐区温度监测");
        when(service.listMonitoringPoints()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/monitoring/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("MP-01"));
    }

    @Test
    void listMonitoringAlarms_returnsAlarms() throws Exception {
        MonitoringAlarm a = new MonitoringAlarm();
        a.setId("AL-01");
        a.setTitle("压力超限预警");
        when(service.listMonitoringAlarms()).thenReturn(List.of(a));

        mockMvc.perform(get("/api/v1/monitoring/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("AL-01"));
    }

    @Test
    void getFacilityDetail_withNameParam() throws Exception {
        FacilityDetailInfo d = new FacilityDetailInfo();
        d.setFacilityName("乙烯装置");
        d.setHazardSourceCode("HS-01");
        when(service.getFacilityDetail("乙烯装置")).thenReturn(d);

        mockMvc.perform(get("/api/v1/facilities/detail").param("name", "乙烯装置"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.facilityName").value("乙烯装置"))
                .andExpect(jsonPath("$.data.hazardSourceCode").value("HS-01"));
    }

    @Test
    void getFacilityDetail_withoutNameParam() throws Exception {
        FacilityDetailInfo d = new FacilityDetailInfo();
        d.setFacilityName("乙烯装置");
        when(service.getFacilityDetail(null)).thenReturn(d);

        mockMvc.perform(get("/api/v1/facilities/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.facilityName").value("乙烯装置"));
    }
}
