package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.BollardItem;
import com.sinopec.mmsecurity.dto.GateControlItem;
import com.sinopec.mmsecurity.dto.PatrolCameraItem;
import com.sinopec.mmsecurity.dto.PerimeterAlarmDetail;
import com.sinopec.mmsecurity.dto.PersonSearchDetail;
import com.sinopec.mmsecurity.dto.PersonSearchResult;
import com.sinopec.mmsecurity.dto.SecurityEvent;
import com.sinopec.mmsecurity.dto.SecurityTrackSummary;
import com.sinopec.mmsecurity.dto.SecurityTrackTimelineItem;
import com.sinopec.mmsecurity.dto.VehicleSearchDetail;
import com.sinopec.mmsecurity.dto.VehicleSearchResult;
import com.sinopec.mmsecurity.entity.FacBollard;
import com.sinopec.mmsecurity.entity.FacGateControl;
import com.sinopec.mmsecurity.entity.FacPatrolCamera;
import com.sinopec.mmsecurity.entity.FacPerimeterAlarm;
import com.sinopec.mmsecurity.entity.FacPersonSearch;
import com.sinopec.mmsecurity.entity.FacSecurityEvent;
import com.sinopec.mmsecurity.entity.FacSecurityTrack;
import com.sinopec.mmsecurity.entity.FacSecurityTrackMeta;
import com.sinopec.mmsecurity.entity.FacVehicleSearch;
import com.sinopec.mmsecurity.mapper.FacBollardMapper;
import com.sinopec.mmsecurity.mapper.FacGateControlMapper;
import com.sinopec.mmsecurity.mapper.FacPatrolCameraMapper;
import com.sinopec.mmsecurity.mapper.FacPerimeterAlarmMapper;
import com.sinopec.mmsecurity.mapper.FacPersonSearchMapper;
import com.sinopec.mmsecurity.mapper.FacSecurityEventMapper;
import com.sinopec.mmsecurity.mapper.FacSecurityTrackMapper;
import com.sinopec.mmsecurity.mapper.FacSecurityTrackMetaMapper;
import com.sinopec.mmsecurity.mapper.FacVehicleSearchMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SecurityService（纯 Mockito，不启动 Spring 上下文）：
 * 列表/检索字段映射、keyword 服务端模糊匹配（车牌/卡口/状态，忽略大小写）、空 keyword 返回全量。
 */
class SecurityServiceTest {

    private final FacPatrolCameraMapper patrolCameraMapper = mock(FacPatrolCameraMapper.class);
    private final FacGateControlMapper gateControlMapper = mock(FacGateControlMapper.class);
    private final FacBollardMapper bollardMapper = mock(FacBollardMapper.class);
    private final FacVehicleSearchMapper vehicleSearchMapper = mock(FacVehicleSearchMapper.class);
    private final FacPersonSearchMapper personSearchMapper = mock(FacPersonSearchMapper.class);
    private final FacSecurityEventMapper securityEventMapper = mock(FacSecurityEventMapper.class);
    private final FacSecurityTrackMapper trackMapper = mock(FacSecurityTrackMapper.class);
    private final FacSecurityTrackMetaMapper trackMetaMapper = mock(FacSecurityTrackMetaMapper.class);
    private final FacPerimeterAlarmMapper perimeterAlarmMapper = mock(FacPerimeterAlarmMapper.class);
    private final SecurityService service = new SecurityService(
            patrolCameraMapper, gateControlMapper, bollardMapper,
            vehicleSearchMapper, personSearchMapper, securityEventMapper,
            trackMapper, trackMetaMapper, perimeterAlarmMapper);

    @Test
    void listPatrolCameras_mapsFields() {
        FacPatrolCamera e = new FacPatrolCamera();
        e.setId(1L);
        e.setName("北环路1#");
        e.setZone("路网防控");
        e.setStatus("正常");
        e.setLongitude(110.88165);
        e.setLatitude(21.68112);
        when(patrolCameraMapper.selectList(null)).thenReturn(List.of(e));

        List<PatrolCameraItem> r = service.listPatrolCameras();
        assertEquals(1, r.size());
        PatrolCameraItem d = r.get(0);
        assertEquals(1L, d.getId());
        assertEquals("北环路1#", d.getName());
        assertEquals("路网防控", d.getZone());
        assertEquals(110.88165, d.getLongitude());
    }

    @Test
    void listGateControls_mapsFields() {
        FacGateControl e = new FacGateControl();
        e.setId(1L);
        e.setName("1#门-道闸1");
        e.setLocation("1#门");
        e.setStatus("正常");
        when(gateControlMapper.selectList(null)).thenReturn(List.of(e));

        List<GateControlItem> r = service.listGateControls();
        assertEquals(1, r.size());
        assertEquals("1#门", r.get(0).getLocation());
    }

    @Test
    void listBollards_mapsFields() {
        FacBollard e = new FacBollard();
        e.setId(1L);
        e.setName("1#门防恐柱");
        e.setZone("1#门");
        e.setStatus("正常");
        when(bollardMapper.selectList(null)).thenReturn(List.of(e));

        List<BollardItem> r = service.listBollards();
        assertEquals(1, r.size());
        assertEquals("1#门防恐柱", r.get(0).getName());
    }

    @Test
    void searchVehicles_noKeyword_returnsAll() {
        FacVehicleSearch a = new FacVehicleSearch();
        a.setId(1L);
        a.setPlate("粤KA4543");
        a.setStatus("入厂");
        FacVehicleSearch b = new FacVehicleSearch();
        b.setId(2L);
        b.setPlate("未识别");
        b.setStatus("出厂");
        when(vehicleSearchMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageOf(List.of(a, b)));

        List<VehicleSearchResult> r = service.searchVehicles(null);
        assertEquals(2, r.size());
    }

    @Test
    void searchVehicles_keywordFiltersByPlateAndStatus() {
        FacVehicleSearch a = new FacVehicleSearch();
        a.setId(1L);
        a.setPlate("粤KA4543");
        a.setStatus("入厂");
        a.setGate("东门-入");
        FacVehicleSearch b = new FacVehicleSearch();
        b.setId(2L);
        b.setPlate("未识别");
        b.setStatus("出厂");
        b.setGate("南门-出");
        when(vehicleSearchMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageOf(List.of(a, b)));

        List<VehicleSearchResult> byPlate = service.searchVehicles("粤K");
        assertEquals(1, byPlate.size());
        assertEquals("粤KA4543", byPlate.get(0).getPlate());

        List<VehicleSearchResult> byStatus = service.searchVehicles("出厂");
        assertEquals(1, byStatus.size());
        assertEquals("未识别", byStatus.get(0).getPlate());
    }

    @Test
    void searchPersons_keywordFiltersByName() {
        FacPersonSearch a = new FacPersonSearch();
        a.setId(1L);
        a.setName("张三");
        a.setGate("东门-入");
        a.setStatus("入厂");
        when(personSearchMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageOf(List.of(a)));

        List<PersonSearchResult> r = service.searchPersons("张三");
        assertEquals(1, r.size());
        assertEquals("东门-入", r.get(0).getGate());
    }

    private static <T> Page<T> pageOf(List<T> rows) {
        Page<T> p = new Page<>(1, rows.size(), false);
        p.setRecords(rows);
        return p;
    }

    @Test
    void listSecurityEvents_mapsFields() {
        FacSecurityEvent e = new FacSecurityEvent();
        e.setEventId("EVT-20260907-0001");
        e.setPerson("张伟");
        e.setChannel("1#门-道闸1");
        e.setCardId("C1001");
        e.setVehicle("粤K·12345");
        e.setDirection("进");
        e.setLevel(1);
        e.setTs("2026-09-07 08:02:11");
        when(securityEventMapper.selectList(null)).thenReturn(List.of(e));

        List<SecurityEvent> r = service.listSecurityEvents();
        assertEquals(1, r.size());
        SecurityEvent d = r.get(0);
        assertEquals("EVT-20260907-0001", d.getEventId());
        assertEquals("进", d.getDirection());
        assertEquals(1, d.getLevel());
        assertTrue(d.getTs().startsWith("2026-09-07"));
    }

    @Test
    void trackTimeline_mapsAndFallsBackToDefault() {
        FacSecurityTrack e = new FacSecurityTrack();
        e.setId(1L);
        e.setLocation("东门-入");
        e.setStatus("入厂");
        e.setStatusTone("enter");
        e.setTrackTime("2026-01-20 09:12:08");
        e.setCaptureHint("东门卡口");
        when(trackMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(e));

        List<SecurityTrackTimelineItem> r = service.trackTimeline("vehicle", 1L);
        assertEquals(1, r.size());
        assertEquals("东门-入", r.get(0).getLocation());
        assertEquals("enter", r.get(0).getStatusTone());
        assertEquals("2026-01-20 09:12:08", r.get(0).getTime());
    }

    @Test
    void trackSummary_derivesTimeRangeFromTimeline() {
        FacSecurityTrack first = new FacSecurityTrack();
        first.setTrackTime("2026-01-20 09:12:08");
        FacSecurityTrack last = new FacSecurityTrack();
        last.setTrackTime("2026-01-20 10:05:12");
        when(trackMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(first, last));

        FacSecurityTrackMeta meta = new FacSecurityTrackMeta();
        meta.setTrackMode("vehicle");
        meta.setStartLabel("东门");
        meta.setEndLabel("装卸点");
        when(trackMetaMapper.selectById("vehicle")).thenReturn(meta);

        SecurityTrackSummary s = service.trackSummary("vehicle", 1L);
        assertEquals("东门", s.getStartLabel());
        assertEquals("装卸点", s.getEndLabel());
        assertEquals("2026-01-20 09:12:08 - 2026-01-20 10:05:12", s.getTimeRange());
    }

    @Test
    void trackSummary_emptyTimelineUsesDash() {
        when(trackMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        SecurityTrackSummary s = service.trackSummary("person", null);
        assertEquals("—", s.getTimeRange());
    }

    @Test
    void vehicleDetail_mapsExtendedFields() {
        FacVehicleSearch e = new FacVehicleSearch();
        e.setId(1L);
        e.setPlate("粤KA4543");
        e.setConfidence(80);
        e.setGate("东门-入");
        e.setStatus("入厂");
        e.setTime("2026-01-20 10:23:23");
        e.setVehicleType("危化品运输车");
        e.setDriverName("刘师傅");
        e.setDestination("炼油一区装卸点");
        when(vehicleSearchMapper.selectById(1L)).thenReturn(e);

        VehicleSearchDetail d = service.vehicleDetail(1L);
        assertEquals("粤KA4543", d.getPlate());
        assertEquals("危化品运输车", d.getVehicleType());
        assertEquals("刘师傅", d.getDriverName());
        assertEquals("炼油一区装卸点", d.getDestination());
    }

    @Test
    void personDetail_mapsExtendedFields() {
        FacPersonSearch e = new FacPersonSearch();
        e.setId(1L);
        e.setName("张三");
        e.setGate("东门-入");
        e.setStatus("入厂");
        e.setDate("2026-01-20");
        e.setGender("男");
        e.setCompany("茂名石化检修公司");
        e.setSpecialOperation("高处作业");
        when(personSearchMapper.selectById(1L)).thenReturn(e);

        PersonSearchDetail d = service.personDetail(1L);
        assertEquals("张三", d.getName());
        assertEquals("男", d.getGender());
        assertEquals("茂名石化检修公司", d.getCompany());
        assertEquals("高处作业", d.getSpecialOperation());
    }

    @Test
    void detail_unknownIdReturnsNull() {
        when(vehicleSearchMapper.selectById(99L)).thenReturn(null);
        when(personSearchMapper.selectById(99L)).thenReturn(null);
        assertEquals(null, service.vehicleDetail(99L));
        assertEquals(null, service.personDetail(99L));
        assertEquals(null, service.vehicleDetail(null));
    }

    @Test
    void latestPerimeterAlarm_mapsFieldsAndSnapshotPath() {
        FacPerimeterAlarm e = new FacPerimeterAlarm();
        e.setId(1L);
        e.setAlarmCode("AL-20260820-007");
        e.setTitle("周界入侵告警");
        e.setAlarmType("周界");
        e.setSource("周界防范");
        e.setLevelCode("一级");
        e.setStatus("未确认");
        e.setFalseAlarm("未核实");
        e.setAlarmTime("2026-08-20 03:22:48");
        e.setObjectType("区域");
        e.setObjectName("南门西侧周界");
        e.setLocation("厂区南门西侧 200 米");
        e.setDescription("非授权人员翻越周界进入厂区，请立即核实。");
        e.setDeviceType("周界摄像机");
        e.setDeviceId("CAM-PERI-07");
        e.setPoint("南门西侧 200 米");
        e.setIntrusionPosition("南门西侧 200 米");
        e.setIntrusionMethod("翻越围栏");
        e.setRelatedCamera("CAM-PERI-07");
        e.setLongitude(110.8872);
        e.setLatitude(21.6709);
        e.setDispatchPersonnel("王成,赵五");
        e.setNotifyApp(true);
        e.setNotifySms(false);
        e.setRescueEventId(7L);
        e.setMonitorId("cam-peri-07");
        e.setMonitorLabel("南门西侧周界监控");
        e.setSnapshotBytes(new byte[] { 1, 2, 3 });
        when(perimeterAlarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(e));

        PerimeterAlarmDetail d = service.latestPerimeterAlarm();
        assertEquals("AL-20260820-007", d.getAlarmCode());
        assertEquals("一级", d.getLevel());
        assertEquals("2026-08-20 03:22:48", d.getTime());
        assertEquals("CAM-PERI-07", d.getDeviceId());
        assertEquals(110.8872, d.getLongitude());
        assertEquals(List.of("王成", "赵五"), d.getDispatchPersonnel());
        assertEquals(7L, d.getRescueEventId());
        assertEquals("/api/v1/security/perimeter-alarms/1/snapshot", d.getSnapshotPath());
        assertEquals("现场抓拍", d.getSnapshotLabel());
    }

    @Test
    void latestPerimeterAlarm_emptyTableReturnsNull() {
        when(perimeterAlarmMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        assertEquals(null, service.latestPerimeterAlarm());
    }

    @Test
    void perimeterAlarmDetail_splitsChineseCommaAndMarksNoSnapshot() {
        FacPerimeterAlarm e = new FacPerimeterAlarm();
        e.setId(2L);
        e.setDispatchPersonnel("张三，李四");
        when(perimeterAlarmMapper.selectById(2L)).thenReturn(e);

        PerimeterAlarmDetail d = service.perimeterAlarmDetail(2L);
        assertEquals(List.of("张三", "李四"), d.getDispatchPersonnel());
        assertEquals("", d.getSnapshotPath());
        assertEquals("", d.getSnapshotLabel());
        assertEquals(null, service.perimeterAlarmSnapshot(2L));
        assertEquals(null, service.perimeterAlarmDetail(null));
    }

    @Test
    void perimeterAlarmSnapshot_returnsBytesWhenPresent() {
        FacPerimeterAlarm e = new FacPerimeterAlarm();
        e.setId(1L);
        e.setSnapshotBytes(new byte[] { 9, 8, 7 });
        when(perimeterAlarmMapper.selectById(1L)).thenReturn(e);
        assertEquals(3, service.perimeterAlarmSnapshot(1L).length);
        assertEquals(null, service.perimeterAlarmSnapshot(null));
    }
}
