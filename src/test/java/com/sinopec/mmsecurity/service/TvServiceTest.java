package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.TvInspectionSummary;
import com.sinopec.mmsecurity.dto.TvOverview;
import com.sinopec.mmsecurity.entity.FacTvInspectionRecord;
import com.sinopec.mmsecurity.entity.FacTvMonitor;
import com.sinopec.mmsecurity.entity.FacTvOperationStat;
import com.sinopec.mmsecurity.entity.FacTvStatItem;
import com.sinopec.mmsecurity.mapper.FacMajorHazardMapper;
import com.sinopec.mmsecurity.mapper.FacTvInspectionRecordMapper;
import com.sinopec.mmsecurity.mapper.FacTvMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacTvOperationStatMapper;
import com.sinopec.mmsecurity.mapper.FacTvStatItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 工业电视服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class TvServiceTest {

    @Mock
    private FacTvStatItemMapper statItemMapper;
    @Mock
    private FacTvOperationStatMapper operationStatMapper;
    @Mock
    private FacTvInspectionRecordMapper inspectionRecordMapper;
    @Mock
    private FacTvMonitorMapper tvMonitorMapper;
    @Mock
    private FacMajorHazardMapper majorHazardMapper;

    @InjectMocks
    private TvService service;

    @BeforeEach
    void resetCaches() {
        service.clearCaches();
    }

    private static FacTvStatItem statItem(String category, String label, int count, String color, String tone, int sortNo) {
        FacTvStatItem item = new FacTvStatItem();
        item.setItemCategory(category);
        item.setLabel(label);
        item.setItemCount(count);
        item.setColor(color);
        item.setTone(tone);
        item.setIconIndex(0);
        item.setSortNo(sortNo);
        return item;
    }

    private static FacTvMonitor monitor(boolean online, String integrity) {
        FacTvMonitor m = new FacTvMonitor();
        m.setOnline(online);
        m.setIntegrity(integrity);
        return m;
    }

    @Test
    void overview_splitsCategoriesAndMapsStats() {
        when(statItemMapper.selectList(any())).thenReturn(List.of(
                statItem("OVERVIEW", "重大危险源", 665, null, null, 1),
                statItem("MAINTENANCE", "未接单", 12, null, "grey", 1),
                statItem("EVENT", "区域入侵", 152, "#f0b429", null, 1)));
        when(majorHazardMapper.selectCount(any())).thenReturn(12L);
        when(tvMonitorMapper.selectList(any())).thenReturn(List.of(
                monitor(true, "良好"), monitor(true, "良好"), monitor(false, "故障")));
        FacTvOperationStat stat = new FacTvOperationStat();
        stat.setEventTotal(110);
        when(operationStatMapper.selectList(any())).thenReturn(List.of(stat));

        TvOverview overview = service.overview();

        assertEquals(1, overview.getOverviewItems().size());
        assertEquals(12, overview.getOverviewItems().get(0).getValue(),
                "「重大危险源」改由 fac_major_hazard 计数（与 GET /hazards 同源），不再取手填 665");
        assertEquals("grey", overview.getMaintenanceOrders().get(0).getTone());
        assertEquals("#f0b429", overview.getEventBreakdown().get(0).getColor());
        assertEquals(3, overview.getOperationStats().getTotal(), "运行统计改由 fac_tv_monitor 明细聚合");
        assertEquals(1, overview.getOperationStats().getOffline());
        assertEquals(1, overview.getOperationStats().getFault());
        assertEquals(67, overview.getOperationStats().getOnlineRate(), "(3-1)/3 = 66.7% → 67");
        assertEquals(67, overview.getOperationStats().getIntegrityRate());
        assertEquals(110, overview.getOperationStats().getEventTotal(), "eventTotal 无明细源，沿用统计表原值");
    }

    @Test
    void overview_handlesMissingStatRow() {
        when(statItemMapper.selectList(any())).thenReturn(List.of());
        when(operationStatMapper.selectList(any())).thenReturn(List.of());
        when(tvMonitorMapper.selectList(any())).thenReturn(List.of());
        when(majorHazardMapper.selectCount(any())).thenReturn(0L);

        TvOverview overview = service.overview();

        assertEquals(0, overview.getOverviewItems().size());
        assertEquals(0, overview.getOperationStats().getTotal(), "无监测点时 total 为 0 而非 null");
        assertEquals(0, overview.getOperationStats().getEventTotal());
    }

    @Test
    void inspections_splitsVehiclesAndPersons() {
        FacTvInspectionRecord vehicle = new FacTvInspectionRecord();
        vehicle.setId(1L);
        vehicle.setRecordKind("VEHICLE");
        vehicle.setAreaCode("refinery");
        vehicle.setSubjectName("粤KAA543");
        vehicle.setBadge("入厂");
        vehicle.setGateName("3#门-入");
        vehicle.setRecordTime("2026-03-17 10:22:23");
        vehicle.setSortNo(1);
        FacTvInspectionRecord person = new FacTvInspectionRecord();
        person.setId(9L);
        person.setRecordKind("PERSON");
        person.setAreaCode("refinery");
        person.setSubjectName("陈志强");
        person.setBadge("员工");
        person.setDepartment("炼油运行一部");
        person.setGateName("3#门-入");
        person.setRecordTime("10:21:18");
        person.setSortNo(1);
        when(inspectionRecordMapper.selectList(any())).thenReturn(List.of(vehicle, person));

        TvInspectionSummary summary = service.inspections();

        assertEquals(1, summary.getVehicles().size());
        assertEquals("粤KAA543", summary.getVehicles().get(0).getPlate());
        assertNull(summary.getVehicles().get(0).getName());
        assertEquals(1, summary.getPersons().size());
        assertEquals("陈志强", summary.getPersons().get(0).getName());
        assertEquals("炼油运行一部", summary.getPersons().get(0).getDepartment());
    }
}
