package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.TyphoonAuxItem;
import com.sinopec.mmsecurity.dto.TyphoonDispatchResource;
import com.sinopec.mmsecurity.dto.TyphoonEmergencyIncident;
import com.sinopec.mmsecurity.entity.FacTyphoonAuxItem;
import com.sinopec.mmsecurity.entity.FacTyphoonDispatchResource;
import com.sinopec.mmsecurity.entity.FacTyphoonEventInfo;
import com.sinopec.mmsecurity.entity.FacTyphoonIncident;
import com.sinopec.mmsecurity.entity.FacTyphoonLiveVideo;
import com.sinopec.mmsecurity.entity.FacTyphoonMapRiskPoint;
import com.sinopec.mmsecurity.entity.FacTyphoonMonitorObject;
import com.sinopec.mmsecurity.entity.FacTyphoonRiskWarning;
import com.sinopec.mmsecurity.entity.FacTyphoonSeries;
import com.sinopec.mmsecurity.entity.SysDutyMember;
import com.sinopec.mmsecurity.entity.SysKnowledgeItem;
import com.sinopec.mmsecurity.mapper.FacTyphoonAuxItemMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonDispatchResourceMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonEventInfoMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonIncidentMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonLiveVideoMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonMapRiskPointMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonMonitorObjectMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonRiskWarningMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonSeriesMapper;
import com.sinopec.mmsecurity.mapper.SysDutyMemberMapper;
import com.sinopec.mmsecurity.mapper.SysKnowledgeItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TyphoonEmergencyServiceTest {

    @Mock
    private FacTyphoonIncidentMapper incidentMapper;
    @Mock
    private FacTyphoonMonitorObjectMapper monitorObjectMapper;
    @Mock
    private FacTyphoonRiskWarningMapper riskWarningMapper;
    @Mock
    private FacTyphoonLiveVideoMapper liveVideoMapper;
    @Mock
    private FacTyphoonMapRiskPointMapper mapRiskPointMapper;
    @Mock
    private FacTyphoonSeriesMapper seriesMapper;
    @Mock
    private FacTyphoonEventInfoMapper eventInfoMapper;
    @Mock
    private FacTyphoonAuxItemMapper auxItemMapper;
    @Mock
    private FacTyphoonDispatchResourceMapper dispatchResourceMapper;
    @Mock
    private SysDutyMemberMapper dutyMemberMapper;
    @Mock
    private SysKnowledgeItemMapper knowledgeItemMapper;

    @InjectMocks
    private TyphoonEmergencyService service;

    private FacTyphoonIncident defaultIncident() {
        FacTyphoonIncident i = new FacTyphoonIncident();
        i.setId(1L);
        i.setEventId(100L);
        i.setTitle("台风沙迦防台防汛工作");
        i.setLocation("全厂范围");
        i.setLongitude(110.92);
        i.setLatitude(21.67);
        i.setStartedAt("2026-06-25T08:12:00");
        i.setStatusName("processing");
        i.setMeteorologySummary("受台风外围云系影响");
        i.setWaterLevelWarn(0.6);
        i.setWaterLevelDanger(0.8);
        i.setTyphoonApiCode("202518");
        i.setIsDefault(true);
        return i;
    }

    private void stubSubTables() {
        FacTyphoonMonitorObject mo = new FacTyphoonMonitorObject();
        mo.setObjCode("rainfall");
        mo.setObjName("24h 降雨量");
        mo.setObjValue("12.5");
        mo.setUnit("mm");
        mo.setStatusName("normal");
        mo.setStatusText("正常");
        when(monitorObjectMapper.selectList(any())).thenReturn(List.of(mo));

        FacTyphoonRiskWarning rw = new FacTyphoonRiskWarning();
        rw.setWarnCode("w1");
        rw.setWarnTime("08:12");
        rw.setWarnType("蓝色预警");
        rw.setContent("防台防汛蓝色预警");
        when(riskWarningMapper.selectList(any())).thenReturn(List.of(rw));

        FacTyphoonLiveVideo lv = new FacTyphoonLiveVideo();
        lv.setVideoCode("v1");
        lv.setVideoLabel("化工区摄像头");
        lv.setSceneIndex(0);
        lv.setAngle("全景");
        lv.setStatusName("online");
        lv.setDeviceCode("cam-01");
        when(liveVideoMapper.selectList(any())).thenReturn(List.of(lv));

        FacTyphoonMapRiskPoint rp = new FacTyphoonMapRiskPoint();
        rp.setPointCode("p1");
        rp.setPointName("低洼积水点A");
        rp.setLongitude(110.93);
        rp.setLatitude(21.68);
        rp.setStatusName("risk");
        rp.setStatusText("高风险");
        rp.setResponsibleUnit("安全环保部");
        rp.setPredeployed(true);
        rp.setDeployment("已预置抽水泵2台");
        rp.setLabelOffsetX(20);
        rp.setLabelOffsetY(-12);
        rp.setClusterCount(3);
        rp.setKind("risk");
        rp.setVideoIds("v1, v2");
        when(mapRiskPointMapper.selectList(any())).thenReturn(List.of(rp));

        FacTyphoonSeries s1 = new FacTyphoonSeries();
        s1.setSeriesKey("precipitation");
        s1.setPointLabel("08:00");
        s1.setPointValue(5.0);
        s1.setSortNo(1);
        FacTyphoonSeries s2 = new FacTyphoonSeries();
        s2.setSeriesKey("wind");
        s2.setPointLabel("08:00");
        s2.setPointValue(12.0);
        s2.setSortNo(1);
        FacTyphoonSeries s3 = new FacTyphoonSeries();
        s3.setSeriesKey("waterLevel");
        s3.setPointLabel("08:00");
        s3.setPointValue(0.4);
        s3.setSortNo(1);
        when(seriesMapper.selectList(any())).thenReturn(List.of(s1, s2, s3));

        FacTyphoonEventInfo ei = new FacTyphoonEventInfo();
        ei.setFieldLabel("影响范围");
        ei.setFieldValue("全厂");
        when(eventInfoMapper.selectList(any())).thenReturn(List.of(ei));

        FacTyphoonAuxItem ai = new FacTyphoonAuxItem();
        ai.setId(1L);
        ai.setLine1("应急物资");
        ai.setLine2("已到位");
        ai.setItemCount(8);
        ai.setCountTone("cyan");
        ai.setIconIndex(0);
        when(auxItemMapper.selectList(any())).thenReturn(List.of(ai));

        SysDutyMember dm = new SysDutyMember();
        dm.setId(1L);
        dm.setName("张伟");
        dm.setRole("总指挥");
        dm.setPhone("13800000000");
        when(dutyMemberMapper.selectList(any())).thenReturn(List.of(dm));

        SysKnowledgeItem ki = new SysKnowledgeItem();
        ki.setId(1L);
        ki.setTitle("防台防汛应急预案");
        ki.setCount(3);
        when(knowledgeItemMapper.selectList(any())).thenReturn(List.of(ki));

        FacTyphoonDispatchResource dr = new FacTyphoonDispatchResource();
        dr.setResourceCode("r1");
        dr.setResourceType("救援队伍");
        dr.setResourceName("消防救援队");
        dr.setCode("XF-01");
        dr.setOrganization("消防支队");
        dr.setArea("厂区");
        dr.setStatusName("可调度");
        dr.setDistanceKm(1.2);
        dr.setEtaMinutes(5);
        dr.setCapacity("30人");
        dr.setContact("李强");
        dr.setPhone("13900000000");
        dr.setLongitude(110.90);
        dr.setLatitude(21.66);
        when(dispatchResourceMapper.selectList(any())).thenReturn(List.of(dr));
    }

    @Test
    void incident_default_assemblesAllSections() {
        when(incidentMapper.selectOne(any())).thenReturn(defaultIncident());
        stubSubTables();

        TyphoonEmergencyIncident dto = service.incident(null);

        assertNotNull(dto);
        assertEquals(100L, dto.getEventId());
        assertEquals("台风沙迦防台防汛工作", dto.getTitle());
        assertEquals(0.6, dto.getWaterLevelWarn());
        assertEquals(1, dto.getMonitoringObjects().size());
        assertEquals("24h 降雨量", dto.getMonitoringObjects().get(0).getName());
        assertEquals(1, dto.getRiskWarnings().size());
        assertEquals(1, dto.getLiveVideos().size());
        assertEquals(1, dto.getMapRiskPoints().size());
        assertEquals(List.of("v1", "v2"), dto.getMapRiskPoints().get(0).getVideoIds());
        assertEquals(1, dto.getEventInfoFields().size());
        assertEquals(1, dto.getAuxiliaryItems().size());
        assertEquals(1, dto.getDutyPersons().size());
        assertEquals("张伟", dto.getDutyPersons().get(0).getName());
        assertEquals(1, dto.getPrecipitationSeries().size());
        assertEquals(1, dto.getWindSpeedSeries().size());
        assertEquals(1, dto.getWaterLevelSeries().size());
        assertEquals(1, dto.getWeatherChartLabels().size());
        assertEquals(1, dto.getWaterLevelLabels().size());
    }

    @Test
    void incident_byEventId_usesEventRow() {
        FacTyphoonIncident eventRow = defaultIncident();
        eventRow.setId(2L);
        eventRow.setEventId(100L);
        when(incidentMapper.selectOne(any())).thenReturn(eventRow);
        stubSubTables();

        TyphoonEmergencyIncident dto = service.incident(100L);

        assertNotNull(dto);
        assertEquals(100L, dto.getEventId());
        assertEquals(1, dto.getMapRiskPoints().size());
        assertEquals("张伟", dto.getDutyPersons().get(0).getName());
    }

    @Test
    void incident_unknownEventId_fallsBackToDefault() {
        // 第一次 selectOne(firstByEventId) 返回 null，第二次 selectOne(firstDefault) 返回默认事件
        when(incidentMapper.selectOne(any())).thenReturn(null, defaultIncident());
        stubSubTables();

        TyphoonEmergencyIncident dto = service.incident(999L);

        assertNotNull(dto);
        assertEquals("台风沙迦防台防汛工作", dto.getTitle());
    }

    @Test
    void incident_noIncidentAtAll_returnsNull() {
        when(incidentMapper.selectOne(any())).thenReturn(null);

        TyphoonEmergencyIncident dto = service.incident(null);

        assertNull(dto);
    }

    @Test
    void dispatchResources_mapsAllFields() {
        stubSubTables();

        List<TyphoonDispatchResource> list = service.dispatchResources();

        assertEquals(1, list.size());
        TyphoonDispatchResource r = list.get(0);
        assertEquals("r1", r.getId());
        assertEquals("消防救援队", r.getName());
        assertEquals("可调度", r.getStatus());
        assertEquals(1.2, r.getDistanceKm());
    }

    @Test
    void knowledgeAuxItems_mapsKnowledgeEntries() {
        stubSubTables();

        List<TyphoonAuxItem> list = service.knowledgeAuxItems();

        assertEquals(1, list.size());
        assertEquals("防台防汛应急预案", list.get(0).getLine1());
        assertEquals("cyan", list.get(0).getCountTone());
    }

    @Test
    void mapRiskPoint_nullVideoIds_yieldsEmptyList() {
        FacTyphoonMapRiskPoint rp = new FacTyphoonMapRiskPoint();
        rp.setPointCode("p2");
        rp.setPointName("测试点");
        rp.setLongitude(110.0);
        rp.setLatitude(21.0);
        rp.setStatusName("normal");
        rp.setPredeployed(false);
        rp.setKind("resource");
        rp.setVideoIds(null);
        when(mapRiskPointMapper.selectList(any())).thenReturn(List.of(rp));
        when(incidentMapper.selectOne(any())).thenReturn(defaultIncident());
        // 其余子表空列表即可
        when(monitorObjectMapper.selectList(any())).thenReturn(List.of());
        when(riskWarningMapper.selectList(any())).thenReturn(List.of());
        when(liveVideoMapper.selectList(any())).thenReturn(List.of());
        when(seriesMapper.selectList(any())).thenReturn(List.of());
        when(eventInfoMapper.selectList(any())).thenReturn(List.of());
        when(auxItemMapper.selectList(any())).thenReturn(List.of());
        when(dutyMemberMapper.selectList(any())).thenReturn(List.of());
        when(knowledgeItemMapper.selectList(any())).thenReturn(List.of());

        TyphoonEmergencyIncident dto = service.incident(null);

        assertNotNull(dto);
        assertTrue(dto.getMapRiskPoints().get(0).getVideoIds().isEmpty());
        assertTrue(dto.getMonitoringObjects().isEmpty());
    }
}
