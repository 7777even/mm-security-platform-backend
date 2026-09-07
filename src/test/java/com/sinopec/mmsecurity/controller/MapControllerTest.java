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
}
