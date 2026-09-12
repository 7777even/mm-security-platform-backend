package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.PersonnelMarker;
import com.sinopec.mmsecurity.dto.ProductionAlarmItem;
import com.sinopec.mmsecurity.dto.ProductionAreaDetail;
import com.sinopec.mmsecurity.dto.ProductionDevicePage;
import com.sinopec.mmsecurity.dto.ProductionOverview;
import com.sinopec.mmsecurity.dto.RiskWarningItem;
import com.sinopec.mmsecurity.entity.FacProductionAlarm;
import com.sinopec.mmsecurity.entity.FacProductionAreaMetric;
import com.sinopec.mmsecurity.entity.FacProductionAreaZone;
import com.sinopec.mmsecurity.entity.FacProductionDevice;
import com.sinopec.mmsecurity.entity.FacProductionDeviceCategory;
import com.sinopec.mmsecurity.entity.FacProductionFacility;
import com.sinopec.mmsecurity.entity.FacProductionPersonnel;
import com.sinopec.mmsecurity.entity.FacProductionRiskWarning;
import com.sinopec.mmsecurity.entity.FacProductionStat;
import com.sinopec.mmsecurity.mapper.FacProductionAlarmMapper;
import com.sinopec.mmsecurity.mapper.FacProductionAreaMetricMapper;
import com.sinopec.mmsecurity.mapper.FacProductionAreaZoneMapper;
import com.sinopec.mmsecurity.mapper.FacProductionDeviceCategoryMapper;
import com.sinopec.mmsecurity.mapper.FacProductionDeviceMapper;
import com.sinopec.mmsecurity.mapper.FacProductionFacilityMapper;
import com.sinopec.mmsecurity.mapper.FacProductionPersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacProductionRiskWarningMapper;
import com.sinopec.mmsecurity.mapper.FacProductionStatMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 生产应急服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {

    @Mock
    private FacProductionFacilityMapper facilityMapper;
    @Mock
    private FacProductionDeviceCategoryMapper deviceCategoryMapper;
    @Mock
    private FacProductionStatMapper statMapper;
    @Mock
    private FacProductionAlarmMapper alarmMapper;
    @Mock
    private FacProductionRiskWarningMapper riskWarningMapper;
    @Mock
    private FacProductionPersonnelMapper personnelMapper;
    @Mock
    private FacProductionDeviceMapper deviceMapper;
    @Mock
    private FacProductionAreaMetricMapper areaMetricMapper;
    @Mock
    private FacProductionAreaZoneMapper areaZoneMapper;

    @InjectMocks
    private ProductionService service;

    /** 捕获最近一次查询条件，用于断言过滤参数是否下推到 SQL（Mockito 无法直接断言 SQL 文本）。 */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> ArgumentCaptor<LambdaQueryWrapper<T>> queryCaptor() {
        return ArgumentCaptor.forClass((Class) LambdaQueryWrapper.class);
    }

    private static <T> Page<T> pageOf(List<T> rows) {
        Page<T> p = new Page<>(1, rows.size(), false);
        p.setRecords(rows);
        return p;
    }

    private static FacProductionFacility facility(long id, String name) {
        FacProductionFacility e = new FacProductionFacility();
        e.setId(id);
        e.setName(name);
        e.setItemCount(596);
        e.setImage("image_0008.png");
        e.setSortNo((int) id);
        return e;
    }

    private static FacProductionDeviceCategory category() {
        FacProductionDeviceCategory e = new FacProductionDeviceCategory();
        e.setId(2L);
        e.setName("监测点");
        e.setItemCount(596);
        e.setImage("image_0009.png");
        e.setSortNo(2);
        return e;
    }

    private static FacProductionStat stat() {
        FacProductionStat e = new FacProductionStat();
        e.setId(1L);
        e.setLabel("未处置告警");
        e.setValueText("12");
        e.setUnit("起");
        e.setTrend(8.0);
        e.setTrendUp(Boolean.TRUE);
        e.setIconIndex(1);
        e.setSortNo(2);
        return e;
    }

    private static FacProductionRiskWarning warning(long id, String level) {
        FacProductionRiskWarning e = new FacProductionRiskWarning();
        e.setId(id);
        e.setLocation("乙烯装置区（二）");
        e.setTypeName("高温预警");
        e.setOccurredAt("2026-03-17 02:00:46");
        e.setPerson("王立军");
        e.setPhone("1380255****");
        e.setLevelCode(level);
        e.setLevelLabel("红色");
        e.setSortNo((int) id);
        return e;
    }

    private static FacProductionAlarm alarm(long id) {
        FacProductionAlarm e = new FacProductionAlarm();
        e.setId(id);
        e.setFacilityId(2L);
        e.setTitle("人员跌倒");
        e.setTitleColor("warning");
        e.setLocation("化工区乙烯装置东侧");
        e.setOccurredAt("2026-03-17 14:21:30");
        e.setDescription("A装置区域发现人员跌倒。");
        e.setStatusName("未处置");
        e.setIconIndex(0);
        e.setThumb("person_fall.png");
        e.setSortNo((int) id);
        return e;
    }

    private static FacProductionDevice device(long id, String category, String status) {
        FacProductionDevice e = new FacProductionDevice();
        e.setId(id);
        e.setCategory(category);
        e.setName("催化裂解监测" + id + "#");
        e.setTypeName("气体监测");
        e.setArea("炼油区");
        e.setStatusName(status);
        e.setLongitude(110.8792);
        e.setLatitude(21.6789);
        e.setSortNo((int) id);
        return e;
    }

    @Test
    void overview_aggregatesCardsAndRiskSummary() {
        when(facilityMapper.selectList(any())).thenReturn(List.of(facility(2L, "生产装置")));
        when(deviceCategoryMapper.selectList(any())).thenReturn(List.of(category()));
        when(statMapper.selectList(any())).thenReturn(List.of(stat()));
        when(riskWarningMapper.selectList(any())).thenReturn(List.of(
                warning(1L, "red"), warning(2L, "red"), warning(3L, "orange"), warning(4L, "yellow")));

        ProductionOverview out = service.overview();
        assertEquals(1, out.getFacilities().size());
        assertEquals("生产装置", out.getFacilities().get(0).getName());
        assertEquals(596, out.getFacilities().get(0).getCount());
        assertEquals("监测点", out.getDevices().get(0).getName());
        assertEquals("未处置告警", out.getStats().get(0).getLabel());
        assertEquals("12", out.getStats().get(0).getValue());
        assertEquals("起", out.getStats().get(0).getUnit());
        assertEquals(2, out.getRiskSummary().getRed());
        assertEquals(1, out.getRiskSummary().getOrange());
        assertEquals(1, out.getRiskSummary().getYellow());
    }

    @Test
    void alarms_mapsEntityToContract() {
        when(alarmMapper.selectPage(any(Page.class), any())).thenReturn(pageOf(List.of(alarm(1L))));

        List<ProductionAlarmItem> out = service.alarms(null);
        assertEquals(1, out.size());
        assertEquals("人员跌倒", out.get(0).getTitle());
        assertEquals("warning", out.get(0).getTitleColor());
        assertEquals("2026-03-17 14:21:30", out.get(0).getTime());
        assertEquals("未处置", out.get(0).getStatus());
        assertEquals("person_fall.png", out.get(0).getThumb());
    }

    @Test
    void alarms_facilityIdDrivesFilter() {
        when(alarmMapper.selectPage(any(Page.class), any())).thenReturn(pageOf(List.of(alarm(1L))));

        service.alarms(2L);
        ArgumentCaptor<LambdaQueryWrapper<FacProductionAlarm>> captor = queryCaptor();
        verify(alarmMapper).selectPage(any(Page.class), captor.capture());
        assertTrue(captor.getValue().getTargetSql().contains("facility_id"),
                "facilityId 非空时应在 SQL 下推 facility_id 过滤");

        service.alarms(null);
        verify(alarmMapper, times(2)).selectPage(any(Page.class), captor.capture());
        assertFalse(captor.getValue().getTargetSql().contains("facility_id"),
                "facilityId 为空时不应下推过滤条件");
    }

    @Test
    void riskWarnings_mapsLevelFields() {
        when(riskWarningMapper.selectList(any())).thenReturn(List.of(warning(1L, "red")));

        List<RiskWarningItem> out = service.riskWarnings();
        assertEquals(1, out.size());
        assertEquals("高温预警", out.get(0).getType());
        assertEquals("2026-03-17 02:00:46", out.get(0).getTime());
        assertEquals("red", out.get(0).getLevel());
        assertEquals("红色", out.get(0).getLevelLabel());
        assertEquals("王立军", out.get(0).getPerson());
    }

    @Test
    void personnel_mapsPercentAndCoordinates() {
        FacProductionPersonnel e = new FacProductionPersonnel();
        e.setId(1L);
        e.setLeftRatio("54.2%");
        e.setTopRatio("25.3%");
        e.setLongitude(110.8836);
        e.setLatitude(21.6838);
        e.setLocation("炼化厂区丙侧");
        e.setPersonCount(365);
        e.setMarkerIcon("person_cluster.png");
        e.setPopupBg("#0b2a4a");
        e.setMarkerDot("#3ec6ff");
        e.setMarkerLine("#3ec6ff");
        when(personnelMapper.selectList(any())).thenReturn(List.of(e));

        List<PersonnelMarker> out = service.personnel();
        assertEquals("54.2%", out.get(0).getLeft());
        assertEquals("25.3%", out.get(0).getTop());
        assertEquals(Double.valueOf(110.8836), out.get(0).getLongitude());
        assertEquals(Double.valueOf(21.6838), out.get(0).getLatitude());
        assertEquals(365, out.get(0).getCount());
        assertEquals("#0b2a4a", out.get(0).getPopupBg());
    }

    @Test
    void areaDetail_aggregatesZonesMetricsAndRewritesAlarms() {
        when(facilityMapper.selectById(2L)).thenReturn(facility(2L, "生产装置"));
        FacProductionAreaZone zone = new FacProductionAreaZone();
        zone.setId(1L);
        zone.setFacilityId(2L);
        zone.setZoneCode("a");
        zone.setName("A生产装置");
        zone.setAlarmCount(2);
        zone.setZoneIndex(0);
        when(areaZoneMapper.selectList(any())).thenReturn(List.of(zone));

        FacProductionAreaMetric metric = new FacProductionAreaMetric();
        metric.setId(1L);
        metric.setFacilityId(2L);
        metric.setLabel("重大危险源");
        metric.setValueText("554");
        when(areaMetricMapper.selectList(any())).thenReturn(List.of(metric));
        when(alarmMapper.selectPage(any(Page.class), any())).thenReturn(pageOf(List.of(alarm(1L))));

        ProductionAreaDetail out = service.areaDetail(2L);
        assertEquals(2L, out.getFacilityId());
        assertEquals("生产装置", out.getFacilityName());
        assertEquals("a", out.getZones().get(0).getId());
        assertEquals(2, out.getZones().get(0).getAlarmCount());
        assertEquals("重大危险源", out.getMetrics().get(0).getLabel());
        assertEquals("554", out.getMetrics().get(0).getValue());
        // 人员构成按设施 id 推导：本厂 20+2、承包商 8+2、访客 3+2
        assertEquals(37, out.getPersonnelTotal());
        assertEquals("本厂人员", out.getPersonnelSlices().get(0).getName());
        assertEquals("生产装置区域", out.getAlarms().get(0).getLocation());
        assertEquals("生产装置区域发现人员跌倒。", out.getAlarms().get(0).getDescription());
    }

    @Test
    void areaDetail_whenFacilityMissing_returnsNull() {
        when(facilityMapper.selectById(99L)).thenReturn(null);
        assertNull(service.areaDetail(99L));
    }

    @Test
    void devices_paginatesAndReportsTotal() {
        List<FacProductionDevice> rows = new ArrayList<>();
        for (long i = 1; i <= 35; i++) {
            rows.add(device(i, "监测点", "正常"));
        }
        when(deviceMapper.selectList(any())).thenReturn(rows);

        ProductionDevicePage first = service.devices(null, null, 1, 10);
        assertEquals(1, first.getPage());
        assertEquals(10, first.getSize());
        assertEquals(35L, first.getTotal());
        assertEquals(1L, first.getItems().get(0).getId());

        ProductionDevicePage last = service.devices(null, null, 4, 10);
        assertEquals(5, last.getItems().size());
        assertEquals(35L, last.getItems().get(4).getId());

        // 越界页码返回空列表而非异常
        assertTrue(service.devices(null, null, 9, 10).getItems().isEmpty());
    }

    @Test
    void devices_appliesCategoryAndStatusFilter() {
        when(deviceMapper.selectList(any())).thenReturn(List.of(device(1L, "监测点", "正常")));

        ProductionDevicePage out = service.devices("监测点", "正常", 1, 10);
        assertEquals(1, out.getItems().size());
        assertEquals("监测点", out.getItems().get(0).getCategory());
        assertEquals("正常", out.getItems().get(0).getStatus());
        assertEquals("气体监测", out.getItems().get(0).getType());

        ArgumentCaptor<LambdaQueryWrapper<FacProductionDevice>> captor = queryCaptor();
        verify(deviceMapper).selectList(captor.capture());
        String deviceSql = captor.getValue().getTargetSql();
        assertTrue(deviceSql.contains("category"), "category 应作为过滤条件下推");
        assertTrue(deviceSql.contains("status_name"), "status_name 应作为过滤条件下推");
    }

    @Test
    void devices_treatsAllStatusAndBlankAsNoFilter() {
        when(deviceMapper.selectList(any())).thenReturn(List.of(device(1L, "监测点", "正常")));

        service.devices("  ", "全部状态", 1, 10);
        ArgumentCaptor<LambdaQueryWrapper<FacProductionDevice>> captor = queryCaptor();
        verify(deviceMapper).selectList(captor.capture());
        String sql = captor.getValue().getTargetSql();
        assertFalse(sql.contains("category"), "空白分类不应作为过滤值下推");
        assertFalse(sql.contains("status_name"), "「全部状态」不应作为过滤值下推");
    }

    @Test
    void devices_defaultsPageAndSize() {
        when(deviceMapper.selectList(any())).thenReturn(List.of(device(1L, "监测点", "正常")));

        ProductionDevicePage out = service.devices(null, null, null, null);
        assertEquals(1, out.getPage());
        assertEquals(10, out.getSize());
    }
}
