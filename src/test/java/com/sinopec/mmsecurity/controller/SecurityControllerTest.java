package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.BollardItem;
import com.sinopec.mmsecurity.dto.GateControlItem;
import com.sinopec.mmsecurity.dto.PatrolCameraItem;
import com.sinopec.mmsecurity.dto.PerimeterAlarmDetail;
import com.sinopec.mmsecurity.dto.PersonSearchResult;
import com.sinopec.mmsecurity.dto.SecurityEvent;
import com.sinopec.mmsecurity.dto.VehicleSearchResult;
import com.sinopec.mmsecurity.service.SecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecurityController（standalone MockMvc，不启动 Spring 上下文）：
 * 端点包络结构、字段透传、检索端点的可选 keyword 参数、周界告警抓拍字节端点。
 * 鉴权由 RequireAuthInterceptor 在 WebMvcConfig 注册，standalone 不挂载，故此处不校验登录态。
 */
class SecurityControllerTest {

    private final SecurityService service = mock(SecurityService.class);
    private final SecurityController controller = new SecurityController(service);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void listPatrolCameras_returnsItems() throws Exception {
        PatrolCameraItem d = new PatrolCameraItem();
        d.setId(1L);
        d.setName("北环路1#");
        d.setZone("路网防控");
        when(service.listPatrolCameras()).thenReturn(List.of(d));

        mockMvc.perform(get("/api/v1/security/patrol-cameras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("北环路1#"));
    }

    @Test
    void listGateControls_returnsItems() throws Exception {
        GateControlItem d = new GateControlItem();
        d.setId(1L);
        d.setName("1#门-道闸1");
        d.setLocation("1#门");
        when(service.listGateControls()).thenReturn(List.of(d));

        mockMvc.perform(get("/api/v1/security/gate-controls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].location").value("1#门"));
    }

    @Test
    void listBollards_returnsItems() throws Exception {
        BollardItem d = new BollardItem();
        d.setId(1L);
        d.setName("1#门防恐柱");
        d.setZone("1#门");
        when(service.listBollards()).thenReturn(List.of(d));

        mockMvc.perform(get("/api/v1/security/bollards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("1#门防恐柱"));
    }

    @Test
    void searchVehicles_withKeyword_passesToService() throws Exception {
        VehicleSearchResult d = new VehicleSearchResult();
        d.setId(1L);
        d.setPlate("粤KA4543");
        when(service.searchVehicles("粤K")).thenReturn(List.of(d));

        mockMvc.perform(get("/api/v1/security/search/vehicle").param("keyword", "粤K"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].plate").value("粤KA4543"));
    }

    @Test
    void searchVehicles_withoutKeyword_passesNull() throws Exception {
        VehicleSearchResult d = new VehicleSearchResult();
        d.setId(2L);
        d.setPlate("未识别");
        when(service.searchVehicles(null)).thenReturn(List.of(d));

        mockMvc.perform(get("/api/v1/security/search/vehicle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].plate").value("未识别"));
    }

    @Test
    void searchPersons_withKeyword_passesToService() throws Exception {
        PersonSearchResult d = new PersonSearchResult();
        d.setId(1L);
        d.setName("张三");
        when(service.searchPersons("张三")).thenReturn(List.of(d));

        mockMvc.perform(get("/api/v1/security/search/person").param("keyword", "张三"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("张三"));
    }

    @Test
    void listSecurityEvents_returnsEvents() throws Exception {
        SecurityEvent d = new SecurityEvent();
        d.setEventId("EVT-20260907-0001");
        d.setPerson("张伟");
        d.setDirection("进");
        d.setLevel(1);
        when(service.listSecurityEvents()).thenReturn(List.of(d));

        mockMvc.perform(get("/api/v1/security/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].eventId").value("EVT-20260907-0001"))
                .andExpect(jsonPath("$.data[0].level").value(1));
    }

    @Test
    void latestPerimeterAlarm_returnsDetail() throws Exception {
        PerimeterAlarmDetail d = new PerimeterAlarmDetail();
        d.setId(1L);
        d.setAlarmCode("AL-20260820-007");
        d.setTitle("周界入侵告警");
        d.setStatus("未确认");
        d.setTime("2026-08-20 03:22:48");
        d.setDeviceId("CAM-PERI-07");
        d.setSnapshotPath("/api/v1/security/perimeter-alarms/1/snapshot");
        when(service.latestPerimeterAlarm()).thenReturn(d);

        mockMvc.perform(get("/api/v1/security/perimeter-alarms/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.alarmCode").value("AL-20260820-007"))
                .andExpect(jsonPath("$.data.deviceId").value("CAM-PERI-07"))
                .andExpect(jsonPath("$.data.time").value("2026-08-20 03:22:48"));
    }

    @Test
    void perimeterAlarmById_returnsDetail() throws Exception {
        PerimeterAlarmDetail d = new PerimeterAlarmDetail();
        d.setId(2L);
        d.setStatus("已处理");
        when(service.perimeterAlarmDetail(2L)).thenReturn(d);

        mockMvc.perform(get("/api/v1/security/perimeter-alarms/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.status").value("已处理"));
    }

    @Test
    void perimeterAlarmSnapshot_returnsJpegBytes() throws Exception {
        when(service.perimeterAlarmSnapshot(1L)).thenReturn(new byte[] { (byte) 0xFF, (byte) 0xD8, 1 });

        mockMvc.perform(get("/api/v1/security/perimeter-alarms/1/snapshot"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(new byte[] { (byte) 0xFF, (byte) 0xD8, 1 }));
    }

    @Test
    void perimeterAlarmSnapshot_missingBytes_returns404() throws Exception {
        when(service.perimeterAlarmSnapshot(9L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/security/perimeter-alarms/9/snapshot"))
                .andExpect(status().isNotFound());
    }
}
