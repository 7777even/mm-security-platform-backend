package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.AlarmTrendPoint;
import com.sinopec.mmsecurity.dto.DashboardOverview;
import com.sinopec.mmsecurity.dto.RiskHeatItem;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.service.DashboardService;
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
 * DashboardController（standalone MockMvc，不启动 Spring 上下文）：
 * overview / workstations 返回强类型 DTO，字段与前端契约对齐。
 */
class DashboardControllerTest {

    private final DashboardService service = mock(DashboardService.class);
    private final DashboardController controller = new DashboardController(service);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void overview_returnsDashboardOverview() throws Exception {
        DashboardOverview ov = new DashboardOverview();
        ov.setActiveAlarm(4);
        ov.setDeviceOnline(5);
        ov.setDeviceTotal(8);
        ov.setRiskIndex(3.7);
        ov.setOnlineWorkstation(2);
        when(service.overview()).thenReturn(ov);

        mockMvc.perform(get("/api/v1/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.deviceTotal").value(8))
                .andExpect(jsonPath("$.data.deviceOnline").value(5))
                .andExpect(jsonPath("$.data.activeAlarm").value(4))
                .andExpect(jsonPath("$.data.riskIndex").value(3.7))
                .andExpect(jsonPath("$.data.onlineWorkstation").value(2));
    }

    @Test
    void workstations_returnsWorkstationList() throws Exception {
        Workstation ws = new Workstation();
        ws.setId("WS-01");
        ws.setName("中控室工位-01");
        ws.setZone("罐区A");
        ws.setOnline(true);
        when(service.workstations()).thenReturn(List.of(ws));

        mockMvc.perform(get("/api/v1/dashboard/workstations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value("WS-01"))
                .andExpect(jsonPath("$.data[0].name").value("中控室工位-01"))
                .andExpect(jsonPath("$.data[0].zone").value("罐区A"))
                .andExpect(jsonPath("$.data[0].online").value(true));
    }

    @Test
    void alarmTrend_returnsTrendPoints() throws Exception {
        AlarmTrendPoint p1 = new AlarmTrendPoint();
        p1.setHour("08:00");
        p1.setCount(3);
        AlarmTrendPoint p2 = new AlarmTrendPoint();
        p2.setHour("09:00");
        p2.setCount(12);
        when(service.trend24h(org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class)))
                .thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/dashboard/alarm-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].hour").value("08:00"))
                .andExpect(jsonPath("$.data[0].count").value(3))
                .andExpect(jsonPath("$.data[1].hour").value("09:00"))
                .andExpect(jsonPath("$.data[1].count").value(12));
    }

    @Test
    void riskHeatmap_returnsZoneScores() throws Exception {
        RiskHeatItem it = new RiskHeatItem();
        it.setZone("罐区");
        it.setScore(4.2);
        when(service.riskHeatmap()).thenReturn(List.of(it));

        mockMvc.perform(get("/api/v1/dashboard/risk-heatmap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].zone").value("罐区"))
                .andExpect(jsonPath("$.data[0].score").value(4.2));
    }
}
