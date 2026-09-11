package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.FireMonitorAreaSummary;
import com.sinopec.mmsecurity.dto.FireMonitoredObjectSummary;
import com.sinopec.mmsecurity.dto.FireSituationMarkerSummary;
import com.sinopec.mmsecurity.entity.FacFireMonitorArea;
import com.sinopec.mmsecurity.entity.FacFireMonitoredObject;
import com.sinopec.mmsecurity.entity.FacFireSituationMarker;
import com.sinopec.mmsecurity.mapper.FacFireMonitorAreaMapper;
import com.sinopec.mmsecurity.mapper.FacFireMonitoredObjectMapper;
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

    @Mock
    private FacFireMonitorAreaMapper fireMonitorAreaMapper;

    @Mock
    private FacFireMonitoredObjectMapper fireMonitoredObjectMapper;

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

    private static FacFireMonitorArea area(String code, String scope, String name, String status,
                                          String statusLabel, int equipment, int cameras, int personnel, int sortNo) {
        FacFireMonitorArea row = new FacFireMonitorArea();
        row.setId(1L);
        row.setAreaCode(code);
        row.setScope(scope);
        row.setAreaName(name);
        row.setStatus(status);
        row.setStatusLabel(statusLabel);
        row.setEquipment(equipment);
        row.setCameras(cameras);
        row.setPersonnel(personnel);
        row.setSortNo(sortNo);
        return row;
    }

    @Test
    void areaSummary_mapsAllFieldsAndSortsBySortNo() {
        // 纯 Mockito：ORDER BY 由 DB 执行，mock 按已排序顺序返回（与 DB 行为一致）
        when(fireMonitorAreaMapper.selectList(any())).thenReturn(List.of(
                area("refinery-1", "refinery", "炼油一部装置区", "normal", "运行正常", 128, 24, 16, 1),
                area("refinery-2", "refinery", "储运罐区", "attention", "2台设备离线", 96, 18, 9, 2)));

        FireMonitorAreaSummary summary = service.areaSummary();

        assertEquals(2, summary.getItems().size());
        // 按 sort_no 升序：refinery-1 在前
        assertEquals("refinery-1", summary.getItems().get(0).getId());
        assertEquals("炼油一部装置区", summary.getItems().get(0).getName());
        assertEquals("normal", summary.getItems().get(0).getStatus());
        assertEquals("运行正常", summary.getItems().get(0).getStatusLabel());
        assertEquals(128, summary.getItems().get(0).getEquipment());
        assertEquals(24, summary.getItems().get(0).getCameras());
        assertEquals(16, summary.getItems().get(0).getPersonnel());
        assertEquals("refinery-2", summary.getItems().get(1).getId());
        assertEquals("attention", summary.getItems().get(1).getStatus());
    }

    @Test
    void areaSummary_handlesEmptyTable() {
        when(fireMonitorAreaMapper.selectList(any())).thenReturn(List.of());
        assertEquals(0, service.areaSummary().getItems().size());
    }

    private static FacFireMonitoredObject object(String name, String status, String detail, String tone, int sortNo) {
        FacFireMonitoredObject row = new FacFireMonitoredObject();
        row.setId(1L);
        row.setObjName(name);
        row.setStatus(status);
        row.setDetail(detail);
        row.setTone(tone);
        row.setSortNo(sortNo);
        return row;
    }

    @Test
    void monitoredObjects_mapsAllFields() {
        when(fireMonitoredObjectMapper.selectList(any())).thenReturn(List.of(
                object("A装置区", "告警", "1起火灾告警处置中", "danger", 1),
                object("储运罐区", "预警", "1项特级动火作业", "warning", 2)));

        FireMonitoredObjectSummary summary = service.monitoredObjects();

        assertEquals(2, summary.getItems().size());
        assertEquals("A装置区", summary.getItems().get(0).getName());
        assertEquals("告警", summary.getItems().get(0).getStatus());
        assertEquals("1起火灾告警处置中", summary.getItems().get(0).getDetail());
        assertEquals("danger", summary.getItems().get(0).getTone());
        assertEquals("warning", summary.getItems().get(1).getTone());
    }

    @Test
    void monitoredObjects_handlesEmptyTable() {
        when(fireMonitoredObjectMapper.selectList(any())).thenReturn(List.of());
        assertEquals(0, service.monitoredObjects().getItems().size());
    }
}
