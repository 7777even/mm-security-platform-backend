package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.BollardItem;
import com.sinopec.mmsecurity.dto.BollardWriteRequest;
import com.sinopec.mmsecurity.dto.GateControlItem;
import com.sinopec.mmsecurity.dto.GateControlWriteRequest;
import com.sinopec.mmsecurity.dto.PatrolCameraItem;
import com.sinopec.mmsecurity.dto.PerimeterAlarmDetail;
import com.sinopec.mmsecurity.dto.PersonSearchDetail;
import com.sinopec.mmsecurity.dto.PersonSearchResult;
import com.sinopec.mmsecurity.dto.SecurityEvent;
import com.sinopec.mmsecurity.dto.VehicleSearchDetail;
import com.sinopec.mmsecurity.dto.VehicleSearchResult;
import com.sinopec.mmsecurity.service.SecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void createPerson_returnsDetail() throws Exception {
        PersonSearchDetail detail = new PersonSearchDetail();
        detail.setId(11L);
        detail.setName("张三");
        when(service.createPerson(any())).thenReturn(detail);

        mockMvc.perform(post("/api/v1/security/search/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"张三\",\"gate\":\"东门\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(11))
                .andExpect(jsonPath("$.data.name").value("张三"));
    }

    @Test
    void createPerson_missingName_returnsB3ParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/security/search/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gate\":\"东门\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID));
    }

    @Test
    void deletePerson_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/security/search/person/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(service).deletePerson(11L);
    }

    @Test
    void deletePerson_notFound_returnsB3NotFound() throws Exception {
        doThrow(new BusinessException(ResultCode.NOT_FOUND, "人员登记不存在：99"))
                .when(service).deletePerson(99L);

        mockMvc.perform(delete("/api/v1/security/search/person/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.NOT_FOUND));
    }

    @Test
    void createVehicle_returnsDetail() throws Exception {
        VehicleSearchDetail detail = new VehicleSearchDetail();
        detail.setId(21L);
        detail.setPlate("粤K12345");
        when(service.createVehicle(any())).thenReturn(detail);

        mockMvc.perform(post("/api/v1/security/search/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"粤K12345\",\"gate\":\"南门\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(21))
                .andExpect(jsonPath("$.data.plate").value("粤K12345"));
    }

    @Test
    void createVehicle_missingPlate_returnsB3ParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/security/search/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gate\":\"南门\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID));
    }

    @Test
    void deleteVehicle_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/security/search/vehicle/21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(service).deleteVehicle(21L);
    }

    @Test
    void deleteVehicle_notFound_returnsB3NotFound() throws Exception {
        doThrow(new BusinessException(ResultCode.NOT_FOUND, "车辆登记不存在：99"))
                .when(service).deleteVehicle(99L);

        mockMvc.perform(delete("/api/v1/security/search/vehicle/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.NOT_FOUND));
    }

    @Test
    void deletePerimeterAlarm_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/security/perimeter-alarms/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(service).deletePerimeterAlarm(31L);
    }

    @Test
    void deletePerimeterAlarm_notFound_returnsB3NotFound() throws Exception {
        doThrow(new BusinessException(ResultCode.NOT_FOUND, "周界入侵告警不存在：99"))
                .when(service).deletePerimeterAlarm(99L);

        mockMvc.perform(delete("/api/v1/security/perimeter-alarms/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.NOT_FOUND));
    }

    @Test
    void createGate_returnsItem() throws Exception {
        GateControlItem item = new GateControlItem();
        item.setId(41L);
        item.setName("1#门-道闸1");
        item.setLocation("1#门");
        when(service.createGate(any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/security/gate-controls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"1#门-道闸1\",\"location\":\"1#门\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(41))
                .andExpect(jsonPath("$.data.name").value("1#门-道闸1"));
    }

    @Test
    void createGate_missingName_returnsB3ParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/security/gate-controls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"1#门\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID));
    }

    @Test
    void deleteGate_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/security/gate-controls/41"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(service).deleteGate(41L);
    }

    @Test
    void deleteGate_notFound_returnsB3NotFound() throws Exception {
        doThrow(new BusinessException(ResultCode.NOT_FOUND, "道闸不存在：99"))
                .when(service).deleteGate(99L);

        mockMvc.perform(delete("/api/v1/security/gate-controls/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.NOT_FOUND));
    }

    @Test
    void createBollard_returnsItem() throws Exception {
        BollardItem item = new BollardItem();
        item.setId(51L);
        item.setName("1#门防恐柱");
        item.setZone("1#门");
        when(service.createBollard(any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/security/bollards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"1#门防恐柱\",\"zone\":\"1#门\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(51))
                .andExpect(jsonPath("$.data.name").value("1#门防恐柱"));
    }

    @Test
    void createBollard_missingName_returnsB3ParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/security/bollards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"zone\":\"1#门\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_INVALID));
    }

    @Test
    void deleteBollard_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/security/bollards/51"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(service).deleteBollard(51L);
    }

    @Test
    void deleteBollard_notFound_returnsB3NotFound() throws Exception {
        doThrow(new BusinessException(ResultCode.NOT_FOUND, "防恐柱不存在：99"))
                .when(service).deleteBollard(99L);

        mockMvc.perform(delete("/api/v1/security/bollards/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.NOT_FOUND));
    }
}
