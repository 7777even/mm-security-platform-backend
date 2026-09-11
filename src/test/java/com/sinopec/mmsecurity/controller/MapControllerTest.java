package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.GeoJsonFeature;
import com.sinopec.mmsecurity.dto.GeoJsonFeatureCollection;
import com.sinopec.mmsecurity.dto.GeoJsonGeometry;
import com.sinopec.mmsecurity.service.MapService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MapController（standalone MockMvc，不启动 Spring 上下文）：两点位返回 GeoJSON FeatureCollection。
 */
class MapControllerTest {

    private final MapService service = mock(MapService.class);
    private final MapController controller = new MapController(service);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private GeoJsonFeatureCollection sample() {
        GeoJsonFeatureCollection fc = new GeoJsonFeatureCollection();
        GeoJsonFeature f = new GeoJsonFeature();
        GeoJsonGeometry g = new GeoJsonGeometry();
        g.setType("Point");
        g.setCoordinates(List.of(110.921, 21.663));
        f.setGeometry(g);
        f.setProperties(Map.of("alarmId", "AE-2026-001", "status", "ACTIVE"));
        fc.setFeatures(List.of(f));
        return fc;
    }

    @Test
    void alarmPoints_returnsFeatureCollection() throws Exception {
        when(service.alarmPoints()).thenReturn(sample());
        mockMvc.perform(get("/api/v1/map/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.data.features[0].geometry.coordinates[0]").value(110.921))
                .andExpect(jsonPath("$.data.features[0].properties.alarmId").value("AE-2026-001"));
    }

    @Test
    void devicePoints_returnsFeatureCollection() throws Exception {
        when(service.devicePoints()).thenReturn(sample());
        mockMvc.perform(get("/api/v1/map/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.features[0].properties.alarmId").value("AE-2026-001"));
    }

    @Test
    void zoneSigns_returnsPopupsAndTealTags() throws Exception {
        com.sinopec.mmsecurity.dto.MapZoneSigns signs = new com.sinopec.mmsecurity.dto.MapZoneSigns();
        com.sinopec.mmsecurity.dto.MapZoneSignPopup popup =
                new com.sinopec.mmsecurity.dto.MapZoneSignPopup();
        popup.setTitle("反应器");
        popup.setLocation("储罐区B-3");
        popup.setStatus("异常");
        popup.setStatusLevel("alert");
        com.sinopec.mmsecurity.dto.MapZoneSignTealTag teal =
                new com.sinopec.mmsecurity.dto.MapZoneSignTealTag();
        teal.setTitle("储罐区");
        teal.setStatus("液位正常");
        teal.setValue("85%");
        signs.setPopups(List.of(popup));
        signs.setTealTags(List.of(teal));
        when(service.zoneSigns()).thenReturn(signs);

        mockMvc.perform(get("/api/v1/map/zone-signs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.popups[0].title").value("反应器"))
                .andExpect(jsonPath("$.data.popups[0].location").value("储罐区B-3"))
                .andExpect(jsonPath("$.data.popups[0].statusLevel").value("alert"))
                .andExpect(jsonPath("$.data.tealTags[0].value").value("85%"));
    }
}
