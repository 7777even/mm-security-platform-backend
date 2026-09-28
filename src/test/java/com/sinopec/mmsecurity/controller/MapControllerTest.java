package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.entity.FacMapZoneSign;
import com.sinopec.mmsecurity.entity.FacMonitoringPoint;
import com.sinopec.mmsecurity.mapper.FacMapZoneSignMapper;
import com.sinopec.mmsecurity.mapper.FacMonitoringPointMapper;
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
 * MapController（standalone MockMvc）：报警/设备点位从 fac_monitoring_point 按状态拆分，
 * 返回 GeoJSON FeatureCollection，坐标 [lng,lat]。
 */
class MapControllerTest {

    private final FacMonitoringPointMapper mapper = mock(FacMonitoringPointMapper.class);
    private final FacMapZoneSignMapper zoneSignMapper = mock(FacMapZoneSignMapper.class);
    private final MapController controller = new MapController(mapper, zoneSignMapper);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static FacMonitoringPoint point(String id, String name, String category, String status,
                                            Double lng, Double lat) {
        FacMonitoringPoint p = new FacMonitoringPoint();
        p.setId(id);
        p.setName(name);
        p.setCategory(category);
        p.setStatus(status);
        p.setOrg("乙烯装置区");
        p.setLastTime("2026-09-08T16:00:00Z");
        p.setLongitude(lng);
        p.setLatitude(lat);
        return p;
    }

    @Test
    void alarms_returnsOnlyAlarmingPointsWithLevel() throws Exception {
        when(mapper.selectList(null)).thenReturn(List.of(
                point("mp-6", "A-06液位", "液位", "alarm", 110.8869, 21.6747),
                point("mp-3", "A-03气体检测", "气体检测", "warning", 110.8886, 21.6754),
                point("mp-1", "A-01DCS监测", "DCS", "normal", 110.8899, 21.6769)
        ));

        mockMvc.perform(get("/api/v1/map/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.data.features.length()").value(2))
                .andExpect(jsonPath("$.data.features[0].properties.alarmId").value("mp-6"))
                .andExpect(jsonPath("$.data.features[0].properties.level").value(3))
                .andExpect(jsonPath("$.data.features[1].properties.alarmId").value("mp-3"))
                .andExpect(jsonPath("$.data.features[1].properties.level").value(2))
                .andExpect(jsonPath("$.data.features[0].geometry.coordinates[0]").value(110.8869))
                .andExpect(jsonPath("$.data.features[0].geometry.coordinates[1]").value(21.6747));
    }

    @Test
    void devices_returnsOnlyNormalPointsOnline() throws Exception {
        when(mapper.selectList(null)).thenReturn(List.of(
                point("mp-1", "A-01DCS监测", "DCS", "normal", 110.8899, 21.6769),
                point("mp-6", "A-06液位", "液位", "alarm", 110.8869, 21.6747)
        ));

        mockMvc.perform(get("/api/v1/map/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.features.length()").value(1))
                .andExpect(jsonPath("$.data.features[0].properties.deviceCode").value("mp-1"))
                .andExpect(jsonPath("$.data.features[0].properties.status").value("ONLINE"))
                .andExpect(jsonPath("$.data.features[0].geometry.coordinates[0]").value(110.8899));
    }

    @Test
    void alarms_emptyWhenNoAlarming() throws Exception {
        when(mapper.selectList(null)).thenReturn(List.of(
                point("mp-1", "A-01DCS监测", "DCS", "normal", 110.8899, 21.6769)
        ));

        mockMvc.perform(get("/api/v1/map/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.features.length()").value(0));
    }

    @Test
    void zoneSigns_splitsAlertAndTealBySortNo() throws Exception {
        when(zoneSignMapper.selectList(null)).thenReturn(List.of(
                alert("zs-1", "反应器", "储罐区B-3", "异常", "alert", 1),
                alert("zs-2", "反应器", "储罐区B-5", "异常", "alert", 2),
                teal("zs-5", "储罐区", "液位正常", "85%", 1),
                teal("zs-6", "装置区", "液位正常", "82%", 2)
        ));

        mockMvc.perform(get("/api/v1/map/zone-signs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.popups.length()").value(2))
                .andExpect(jsonPath("$.data.tealTags.length()").value(2))
                .andExpect(jsonPath("$.data.popups[0].title").value("反应器"))
                .andExpect(jsonPath("$.data.popups[0].location").value("储罐区B-3"))
                .andExpect(jsonPath("$.data.popups[0].status").value("异常"))
                .andExpect(jsonPath("$.data.popups[0].statusLevel").value("alert"))
                .andExpect(jsonPath("$.data.tealTags[0].title").value("储罐区"))
                .andExpect(jsonPath("$.data.tealTags[0].status").value("液位正常"))
                .andExpect(jsonPath("$.data.tealTags[0].value").value("85%"));
    }

    private static FacMapZoneSign alert(String id, String title, String location, String status,
                                        String level, int sortNo) {
        FacMapZoneSign s = new FacMapZoneSign();
        s.setId(Long.valueOf(id.replace("zs-", "")));
        s.setSignKind("ALERT");
        s.setTitle(title);
        s.setLocation(location);
        s.setStatusText(status);
        s.setStatusLevel(level);
        s.setStatValue(null);
        s.setSortNo(sortNo);
        return s;
    }

    private static FacMapZoneSign teal(String id, String title, String status, String value, int sortNo) {
        FacMapZoneSign s = new FacMapZoneSign();
        s.setSignKind("TEAL");
        s.setTitle(title);
        s.setLocation(null);
        s.setStatusText(status);
        s.setStatusLevel(null);
        s.setStatValue(value);
        s.setSortNo(sortNo);
        return s;
    }
}
