package com.sinopec.mmsecurity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.dto.FacilityDetailInfo;
import com.sinopec.mmsecurity.dto.MajorHazardDetail;
import com.sinopec.mmsecurity.dto.MajorHazardItem;
import com.sinopec.mmsecurity.dto.MonitoringAlarm;
import com.sinopec.mmsecurity.dto.MonitoringPoint;
import com.sinopec.mmsecurity.entity.FacFacilityDetail;
import com.sinopec.mmsecurity.entity.FacMajorHazard;
import com.sinopec.mmsecurity.entity.FacMonitoringAlarm;
import com.sinopec.mmsecurity.entity.FacMonitoringPoint;
import com.sinopec.mmsecurity.mapper.FacFacilityDetailMapper;
import com.sinopec.mmsecurity.mapper.FacMajorHazardMapper;
import com.sinopec.mmsecurity.mapper.FacMonitoringAlarmMapper;
import com.sinopec.mmsecurity.mapper.FacMonitoringPointMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * HazardService（纯 Mockito，不启动 Spring 上下文）：
 * 列表/详情字段映射、JSON 列解析为 List&lt;Map&gt;、按名称过滤、缺失与脏数据降级。
 * 使用真实 ObjectMapper 验证 JSON 列解析路径（与契约 MajorHazardDetail / FacilityDetailInfo 对齐）。
 */
class HazardServiceTest {

    private final FacMajorHazardMapper majorHazardMapper = mock(FacMajorHazardMapper.class);
    private final FacMonitoringPointMapper monitoringPointMapper = mock(FacMonitoringPointMapper.class);
    private final FacMonitoringAlarmMapper monitoringAlarmMapper = mock(FacMonitoringAlarmMapper.class);
    private final FacFacilityDetailMapper facilityDetailMapper = mock(FacFacilityDetailMapper.class);
    private final HazardService service = new HazardService(
            majorHazardMapper, monitoringPointMapper, monitoringAlarmMapper, facilityDetailMapper, new ObjectMapper());

    @BeforeEach
    void resetCaches() {
        service.clearCaches();
    }

    private FacMajorHazard sampleHazard() {
        FacMajorHazard e = new FacMajorHazard();
        e.setId(1L);
        e.setName("乙烯球罐区");
        e.setLevel("一级");
        e.setRValue(120.5);
        e.setMonitorCount(12);
        e.setVideoCount(3);
        e.setEnterprise("某石化有限公司");
        e.setCategory("压力容器");
        e.setCode("HAZ-001");
        e.setLongitude(121.5);
        e.setLatitude(31.2);
        e.setCommissionDate("2019-05-01");
        e.setKeyProcess(true);
        e.setInChemicalPark(true);
        e.setContactsJson("[{\"name\":\"张三\",\"phone\":\"13800000000\"}]");
        e.setFilesJson("[{\"name\":\"安全评价报告.pdf\"}]");
        e.setMonitorsJson("[]");
        e.setVideosJson("[]");
        e.setChemicalsJson("[]");
        e.setEvacuationRoutesJson("[]");
        e.setOperationsJson("[]");
        return e;
    }

    @Test
    void listMajorHazards_mapsAllFields() {
        FacMajorHazard e = sampleHazard();
        when(majorHazardMapper.selectList(null)).thenReturn(List.of(e));

        List<MajorHazardItem> result = service.listMajorHazards();

        assertEquals(1, result.size());
        MajorHazardItem item = result.get(0);
        assertEquals(1L, item.getId());
        assertEquals("乙烯球罐区", item.getName());
        assertEquals("一级", item.getLevel());
        assertEquals(120.5, item.getRValue());
        assertEquals(12, item.getMonitorCount());
        assertEquals(3, item.getVideoCount());
        assertEquals("HAZ-001", item.getCode());
        assertEquals(121.5, item.getLongitude());
        assertEquals(31.2, item.getLatitude());
    }

    @Test
    void getMajorHazardDetail_parsesJsonColumns() {
        FacMajorHazard e = sampleHazard();
        when(majorHazardMapper.selectById(1L)).thenReturn(e);

        MajorHazardDetail d = service.getMajorHazardDetail(1L);

        assertEquals(1L, d.getId());
        assertEquals("一级", d.getLevel());
        assertEquals(true, d.getKeyProcess());
        assertEquals(true, d.getInChemicalPark());
        assertEquals(1, d.getContacts().size());
        assertEquals("张三", d.getContacts().get(0).get("name"));
        assertEquals("13800000000", d.getContacts().get(0).get("phone"));
        assertEquals(1, d.getFiles().size());
        assertTrue(d.getMonitors().isEmpty());
    }

    @Test
    void getMajorHazardDetail_notFound_returnsNull() {
        when(majorHazardMapper.selectById(99L)).thenReturn(null);
        assertNull(service.getMajorHazardDetail(99L));
    }

    @Test
    void listMonitoringPoints_mapsFields() {
        FacMonitoringPoint p = new FacMonitoringPoint();
        p.setId("MP-01");
        p.setName("罐区温度监测");
        p.setCategory("温度");
        p.setStatus("正常");
        p.setLastTime("2026-09-08 10:00");
        p.setOrg("储运车间");
        p.setLongitude(121.4);
        p.setLatitude(31.1);
        when(monitoringPointMapper.selectList(null)).thenReturn(List.of(p));

        List<MonitoringPoint> result = service.listMonitoringPoints();
        assertEquals(1, result.size());
        MonitoringPoint d = result.get(0);
        assertEquals("MP-01", d.getId());
        assertEquals("罐区温度监测", d.getName());
        assertEquals("正常", d.getStatus());
        assertEquals("储运车间", d.getOrg());
    }

    @Test
    void listMonitoringAlarms_mapsFields() {
        FacMonitoringAlarm a = new FacMonitoringAlarm();
        a.setId("AL-01");
        a.setTitle("压力超限预警");
        a.setDetail("1号罐压力超过阈值");
        a.setArea("罐区A");
        a.setTime("2026-09-08 09:30");
        a.setLevel("二级");
        when(monitoringAlarmMapper.selectList(null)).thenReturn(List.of(a));

        List<MonitoringAlarm> result = service.listMonitoringAlarms();
        assertEquals(1, result.size());
        MonitoringAlarm d = result.get(0);
        assertEquals("AL-01", d.getId());
        assertEquals("压力超限预警", d.getTitle());
        assertEquals("二级", d.getLevel());
    }

    @Test
    void getFacilityDetail_byName_filtersAndParses() {
        FacFacilityDetail f = new FacFacilityDetail();
        f.setFacilityName("乙烯装置");
        f.setHazardSourceCode("HS-01");
        f.setBasicFieldsJson("[{\"label\":\"设计压力\",\"value\":\"2.5MPa\"}]");
        f.setChemicalFieldsJson("[{\"label\":\"介质\",\"value\":\"乙烯\"}]");
        f.setArchivesJson("[{\"name\":\"验收报告.pdf\"}]");

        FacFacilityDetail other = new FacFacilityDetail();
        other.setFacilityName("聚丙烯装置");
        other.setHazardSourceCode("HS-02");

        when(facilityDetailMapper.selectList(null)).thenReturn(List.of(other, f));

        FacilityDetailInfo d = service.getFacilityDetail("乙烯装置");
        assertEquals("乙烯装置", d.getFacilityName());
        assertEquals("HS-01", d.getHazardSourceCode());
        assertEquals(1, d.getBasicFields().size());
        assertEquals("2.5MPa", d.getBasicFields().get(0).get("value"));
        assertEquals(1, d.getChemicalFields().size());
        assertEquals(1, d.getArchives().size());
    }

    @Test
    void getFacilityDetail_nullName_returnsFirst() {
        FacFacilityDetail f = new FacFacilityDetail();
        f.setFacilityName("乙烯装置");
        f.setHazardSourceCode("HS-01");
        when(facilityDetailMapper.selectList(null)).thenReturn(List.of(f));
        FacilityDetailInfo d = service.getFacilityDetail(null);
        assertEquals("乙烯装置", d.getFacilityName());
    }

    @Test
    void getFacilityDetail_blankName_returnsFirst() {
        FacFacilityDetail f = new FacFacilityDetail();
        f.setFacilityName("乙烯装置");
        f.setHazardSourceCode("HS-01");
        when(facilityDetailMapper.selectList(null)).thenReturn(List.of(f));
        FacilityDetailInfo d = service.getFacilityDetail("  ");
        assertEquals("乙烯装置", d.getFacilityName());
    }

    @Test
    void getFacilityDetail_notFound_returnsNull() {
        when(facilityDetailMapper.selectList(null)).thenReturn(List.of());
        assertNull(service.getFacilityDetail("不存在"));
    }

    @Test
    void getFacilityDetail_invalidJson_returnsEmptyList() {
        FacFacilityDetail f = new FacFacilityDetail();
        f.setFacilityName("乙烯装置");
        f.setHazardSourceCode("HS-01");
        f.setBasicFieldsJson("not-json");
        f.setChemicalFieldsJson(null);
        f.setArchivesJson("");
        when(facilityDetailMapper.selectList(null)).thenReturn(List.of(f));
        FacilityDetailInfo d = service.getFacilityDetail("乙烯装置");
        assertTrue(d.getBasicFields().isEmpty());
        assertTrue(d.getChemicalFields().isEmpty());
        assertTrue(d.getArchives().isEmpty());
    }
}
