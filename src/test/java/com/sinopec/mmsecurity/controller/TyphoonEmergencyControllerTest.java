package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.TyphoonDispatchResource;
import com.sinopec.mmsecurity.dto.TyphoonEmergencyIncident;
import com.sinopec.mmsecurity.service.TyphoonEmergencyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 台风应急接口 standalone 校验：路由、B3 包络与聚合结构（纯 Mockito，不起 Spring 上下文）。 */
class TyphoonEmergencyControllerTest {

    private final TyphoonEmergencyService service = Mockito.mock(TyphoonEmergencyService.class);

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new TyphoonEmergencyController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void incident_returnsAggregateEnvelope() throws Exception {
        Mockito.when(service.incident(Mockito.nullable(Long.class))).thenReturn(sample());

        mvc.perform(get("/api/v1/typhoon/incident"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.eventId").value(100))
                .andExpect(jsonPath("$.data.title").value("台风沙迦防台防汛工作"))
                .andExpect(jsonPath("$.data.monitoringObjects[0].status").value("normal"))
                .andExpect(jsonPath("$.data.riskWarnings[0].type").value("内涝预警"))
                .andExpect(jsonPath("$.data.liveVideos[0].deviceCode").value("FX-R1-01"))
                .andExpect(jsonPath("$.data.mapRiskPoints[0].videoIds[0]").value("r1-east"))
                .andExpect(jsonPath("$.data.eventInfoFields[0].label").value("事件名称"))
                // 集合字段缺失时必须为空数组而非 null，避免前端 .map / .some 空指针
                .andExpect(jsonPath("$.data.precipitationSeries").isArray())
                .andExpect(jsonPath("$.data.dutyPersons").isArray())
                .andExpect(jsonPath("$.data.auxiliaryItems").isArray());
    }

    @Test
    void incident_passesEventIdAsQueryParam() throws Exception {
        Mockito.when(service.incident(Mockito.nullable(Long.class))).thenReturn(sample());

        mvc.perform(get("/api/v1/typhoon/incident").param("eventId", "100"))
                .andExpect(status().isOk());
        Mockito.verify(service).incident(100L);
    }

    @Test
    void incident_returnsNotFoundBusinessCodeWhenNoIncidentConfigured() throws Exception {
        Mockito.when(service.incident(Mockito.nullable(Long.class))).thenReturn(null);

        // B3 约定：非 401/403/409 的业务码保持 HTTP 200，由包络 code 表达失败原因
        mvc.perform(get("/api/v1/typhoon/incident"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void dispatchResources_returnsList() throws Exception {
        TyphoonDispatchResource r = new TyphoonDispatchResource();
        r.setId("flood-team-01");
        r.setType("救援队伍");
        r.setName("炼油防汛抢险一组");
        r.setStatus("可调度");
        r.setDistanceKm(0.7);
        r.setEtaMinutes(4);
        Mockito.when(service.dispatchResources()).thenReturn(java.util.List.of(r));

        mvc.perform(get("/api/v1/typhoon/dispatch-resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("炼油防汛抢险一组"))
                .andExpect(jsonPath("$.data[0].distanceKm").value(0.7))
                .andExpect(jsonPath("$.data[0].etaMinutes").value(4));
    }

    private static TyphoonEmergencyIncident sample() {
        TyphoonEmergencyIncident d = new TyphoonEmergencyIncident();
        d.setEventId(100L);
        d.setTitle("台风沙迦防台防汛工作");
        d.setWaterLevelWarn(0.6);
        d.setWaterLevelDanger(0.8);

        com.sinopec.mmsecurity.dto.TyphoonMonitorObject m =
                new com.sinopec.mmsecurity.dto.TyphoonMonitorObject();
        m.setId("outlet");
        m.setStatus("normal");
        d.setMonitoringObjects(java.util.List.of(m));

        com.sinopec.mmsecurity.dto.TyphoonRiskWarning w =
                new com.sinopec.mmsecurity.dto.TyphoonRiskWarning();
        w.setId("1");
        w.setType("内涝预警");
        d.setRiskWarnings(java.util.List.of(w));

        com.sinopec.mmsecurity.dto.TyphoonLiveVideo v =
                new com.sinopec.mmsecurity.dto.TyphoonLiveVideo();
        v.setId("r1-east");
        v.setDeviceCode("FX-R1-01");
        d.setLiveVideos(java.util.List.of(v));

        com.sinopec.mmsecurity.dto.TyphoonMapRiskPoint p =
                new com.sinopec.mmsecurity.dto.TyphoonMapRiskPoint();
        p.setId("r1");
        p.setVideoIds(java.util.List.of("r1-east"));
        d.setMapRiskPoints(java.util.List.of(p));

        com.sinopec.mmsecurity.dto.TyphoonEventInfoField f =
                new com.sinopec.mmsecurity.dto.TyphoonEventInfoField();
        f.setLabel("事件名称");
        f.setValue("台风沙迦防台防汛工作");
        d.setEventInfoFields(java.util.List.of(f));

        d.setPrecipitationSeries(java.util.List.of(2.0, 4.0));
        return d;
    }
}
