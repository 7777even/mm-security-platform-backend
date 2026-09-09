package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.FireSituationMarkerSummary;
import com.sinopec.mmsecurity.entity.FacFireSituationMarker;
import com.sinopec.mmsecurity.mapper.FacFireSituationMarkerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 火情态势地图点位服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class FireSituationServiceTest {

    @Mock
    private FacFireSituationMarkerMapper fireSituationMarkerMapper;

    @InjectMocks
    private FireSituationService service;

    private static FacFireSituationMarker marker(String code, String kind, String title, String subtitle,
                                                 double longitude, double latitude, boolean important,
                                                 String iconUrl, String levelName, long targetId, int sortNo) {
        FacFireSituationMarker marker = new FacFireSituationMarker();
        marker.setId(1L);
        marker.setMarkerCode(code);
        marker.setMarkerKind(kind);
        marker.setTitle(title);
        marker.setSubtitle(subtitle);
        marker.setLongitude(longitude);
        marker.setLatitude(latitude);
        marker.setImportantFlag(important);
        marker.setIconUrl(iconUrl);
        marker.setLevelName(levelName);
        marker.setTargetId(targetId);
        marker.setSortNo(sortNo);
        return marker;
    }

    @Test
    void markers_mapsCodeToStringIdAndAllFields() {
        when(fireSituationMarkerMapper.selectList(any())).thenReturn(List.of(
                marker("event-1", "event", "当前应急事件", "A装置区火灾处置中", 110.875, 21.6855, true,
                        "/icons/fire-situation/flame.svg", "处置中", 1L, 1),
                marker("op-lift", "operation", "一级吊装作业", "乙烯装置区 · 进行中", 110.8835, 21.6752, false,
                        "/icons/fire-situation/crane.svg", "一级", 3L, 4)));

        FireSituationMarkerSummary summary = service.markers();

        assertEquals(2, summary.getItems().size());
        assertEquals("event-1", summary.getItems().get(0).getId());
        assertEquals("event", summary.getItems().get(0).getKind());
        assertEquals("当前应急事件", summary.getItems().get(0).getTitle());
        assertEquals("A装置区火灾处置中", summary.getItems().get(0).getSubtitle());
        assertEquals(110.875, summary.getItems().get(0).getLongitude());
        assertEquals(21.6855, summary.getItems().get(0).getLatitude());
        assertTrue(summary.getItems().get(0).getImportant());
        assertEquals("/icons/fire-situation/flame.svg", summary.getItems().get(0).getIconUrl());
        assertEquals("处置中", summary.getItems().get(0).getLevel());
        assertEquals(1L, summary.getItems().get(0).getTargetId());
        assertEquals("op-lift", summary.getItems().get(1).getId());
        assertFalse(summary.getItems().get(1).getImportant());
        assertEquals(3L, summary.getItems().get(1).getTargetId());
    }

    @Test
    void markers_keepsNullLevel() {
        when(fireSituationMarkerMapper.selectList(any())).thenReturn(List.of(
                marker("alarm-2", "alarm", "GDS报警", "输油管廊 · 未销警", 110.8902, 21.6752, false,
                        "/icons/fire-situation/gas.svg", null, 2L, 7)));

        FireSituationMarkerSummary summary = service.markers();

        assertEquals(1, summary.getItems().size());
        assertNull(summary.getItems().get(0).getLevel());
        assertEquals("alarm", summary.getItems().get(0).getKind());
    }

    @Test
    void markers_handlesEmptyTable() {
        when(fireSituationMarkerMapper.selectList(any())).thenReturn(List.of());

        FireSituationMarkerSummary summary = service.markers();

        assertEquals(0, summary.getItems().size());
    }
}
