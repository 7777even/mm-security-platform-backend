package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.TvInspectionSummary;
import com.sinopec.mmsecurity.dto.TvOverview;
import com.sinopec.mmsecurity.dto.TvSnapshotAckResult;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestRequest;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestResult;
import com.sinopec.mmsecurity.entity.FacTvInspectionRecord;
import com.sinopec.mmsecurity.entity.FacTvMonitor;
import com.sinopec.mmsecurity.entity.FacTvOperationStat;
import com.sinopec.mmsecurity.entity.FacTvSnapshot;
import com.sinopec.mmsecurity.entity.FacTvStatItem;
import com.sinopec.mmsecurity.mapper.FacMajorHazardMapper;
import com.sinopec.mmsecurity.mapper.FacTvInspectionRecordMapper;
import com.sinopec.mmsecurity.mapper.FacTvMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacTvOperationStatMapper;
import com.sinopec.mmsecurity.mapper.FacTvSnapshotMapper;
import com.sinopec.mmsecurity.mapper.FacTvStatItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private FacTvSnapshotMapper snapshotMapper;
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

    // ===================== 录像截图采集入库闭环（V78） =====================

    @Test
    void submitSnapshot_storesPendingAndDecodesBase64() {
        when(tvMonitorMapper.selectOne(any())).thenReturn(null); // 不回查点位名
        when(snapshotMapper.insert(any(FacTvSnapshot.class))).thenAnswer(inv -> {
            FacTvSnapshot e = inv.getArgument(0);
            e.setId(99L);
            return 1;
        });

        TvSnapshotIngestRequest req = new TvSnapshotIngestRequest();
        req.setMonitorCode("ar-01");
        req.setImageBase64("data:image/jpeg;base64,/9j/4AAQSkZJRg==");

        TvSnapshotIngestResult r = service.submitSnapshot(req);
        assertEquals(99L, r.getId());
        assertEquals("PENDING", r.getReviewStatus());

        ArgumentCaptor<FacTvSnapshot> cap = ArgumentCaptor.forClass(FacTvSnapshot.class);
        verify(snapshotMapper, times(1)).insert(cap.capture());
        FacTvSnapshot e = cap.getValue();
        assertEquals("ar-01", e.getMonitorCode());
        assertEquals("PENDING", e.getReviewStatus());
        assertEquals("DEVICE", e.getSource());
        assertNotNull(e.getSnapshotBytes());
        assertNotNull(e.getCreatedAt());
    }

    @Test
    void ackSnapshot_marksAcked() {
        FacTvSnapshot existing = new FacTvSnapshot();
        existing.setId(5L);
        existing.setReviewStatus("PENDING");
        when(snapshotMapper.selectById(5L)).thenReturn(existing);

        TvSnapshotAckResult r = service.ackSnapshot(5L);
        assertEquals("ACKED", r.getReviewStatus());
        assertEquals("ACKED", existing.getReviewStatus());
        verify(snapshotMapper, times(1)).updateById(existing);
    }

    @Test
    void submitSnapshots_batchesValidAndSkipsInvalid() {
        when(tvMonitorMapper.selectOne(any())).thenReturn(null);
        when(snapshotMapper.insert(any(FacTvSnapshot.class))).thenAnswer(inv -> {
            FacTvSnapshot e = inv.getArgument(0);
            e.setId(1L);
            return 1;
        });

        TvSnapshotIngestRequest good = new TvSnapshotIngestRequest();
        good.setMonitorCode("ar-01");
        good.setImageBase64("data:image/jpeg;base64,/9j/4AAQSkZJRg==");
        TvSnapshotIngestRequest bad = new TvSnapshotIngestRequest();
        bad.setMonitorCode("ar-02");
        bad.setImageBase64(""); // 非法：空图，应被跳过

        int n = service.submitSnapshots(List.of(good, bad));
        assertEquals(1, n, "非法项被跳过，仅合法项入库");
        verify(snapshotMapper, times(1)).insert(any(FacTvSnapshot.class));
    }
}
