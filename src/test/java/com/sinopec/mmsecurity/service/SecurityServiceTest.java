package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.BollardItem;
import com.sinopec.mmsecurity.dto.GateControlItem;
import com.sinopec.mmsecurity.dto.PatrolCameraItem;
import com.sinopec.mmsecurity.dto.PersonSearchResult;
import com.sinopec.mmsecurity.dto.SecurityEvent;
import com.sinopec.mmsecurity.dto.VehicleSearchResult;
import com.sinopec.mmsecurity.entity.FacBollard;
import com.sinopec.mmsecurity.entity.FacGateControl;
import com.sinopec.mmsecurity.entity.FacPatrolCamera;
import com.sinopec.mmsecurity.entity.FacPersonSearch;
import com.sinopec.mmsecurity.entity.FacSecurityEvent;
import com.sinopec.mmsecurity.entity.FacVehicleSearch;
import com.sinopec.mmsecurity.mapper.FacBollardMapper;
import com.sinopec.mmsecurity.mapper.FacGateControlMapper;
import com.sinopec.mmsecurity.mapper.FacPatrolCameraMapper;
import com.sinopec.mmsecurity.mapper.FacPersonSearchMapper;
import com.sinopec.mmsecurity.mapper.FacSecurityEventMapper;
import com.sinopec.mmsecurity.mapper.FacVehicleSearchMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private final SecurityService service = new SecurityService(
            patrolCameraMapper, gateControlMapper, bollardMapper,
            vehicleSearchMapper, personSearchMapper, securityEventMapper);

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
        when(vehicleSearchMapper.selectList(null)).thenReturn(List.of(a, b));

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
        when(vehicleSearchMapper.selectList(null)).thenReturn(List.of(a, b));

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
        when(personSearchMapper.selectList(null)).thenReturn(List.of(a));

        List<PersonSearchResult> r = service.searchPersons("张三");
        assertEquals(1, r.size());
        assertEquals("东门-入", r.get(0).getGate());
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
}
