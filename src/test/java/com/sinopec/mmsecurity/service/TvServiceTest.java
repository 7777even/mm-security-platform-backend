package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.dto.TvInspectionSummary;
import com.sinopec.mmsecurity.dto.TvMonitorSummary;
import com.sinopec.mmsecurity.dto.TvMonitorUpsertRequest;
import com.sinopec.mmsecurity.dto.TvOverview;
import com.sinopec.mmsecurity.dto.TvSnapshotAckResult;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestRequest;
import com.sinopec.mmsecurity.dto.TvSnapshotIngestResult;
import com.sinopec.mmsecurity.dto.TvSnapshotPage;
import com.sinopec.mmsecurity.entity.FacTvInspectionRecord;
import com.sinopec.mmsecurity.entity.FacTvMaintenanceOrder;
import com.sinopec.mmsecurity.entity.FacTvMonitor;
import com.sinopec.mmsecurity.entity.FacTvOperationStat;
import com.sinopec.mmsecurity.entity.FacTvSnapshot;
import com.sinopec.mmsecurity.entity.SysZone;
import com.sinopec.mmsecurity.entity.FacTvStatItem;
import com.sinopec.mmsecurity.mapper.FacMajorHazardMapper;
import com.sinopec.mmsecurity.mapper.FacTvInspectionRecordMapper;
import com.sinopec.mmsecurity.mapper.FacTvMaintenanceOrderMapper;
import com.sinopec.mmsecurity.mapper.FacTvMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacTvOperationStatMapper;
import com.sinopec.mmsecurity.mapper.FacTvSnapshotMapper;
import com.sinopec.mmsecurity.mapper.FacTvStatItemMapper;
import com.sinopec.mmsecurity.mapper.SysZoneMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @Mock
    private SysZoneMapper zoneMapper;
    @Mock
    private FacTvMaintenanceOrderMapper maintenanceOrderMapper;

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

    private static FacTvMonitor monitorCat(boolean online, String integrity, String category) {
        FacTvMonitor m = monitor(online, integrity);
        m.setMonitorCategory(category);
        return m;
    }

    private static FacTvMaintenanceOrder maintOrder(String status) {
        FacTvMaintenanceOrder o = new FacTvMaintenanceOrder();
        o.setId(1L);
        o.setOrderNo("WO-x");
        o.setDeviceName("设备");
        o.setStatus(status);
        o.setCreatedAt("2026-09-28 08:12:33");
        return o;
    }

    @Test
    void overview_splitsCategoriesAndMapsStats() {
        when(statItemMapper.selectList(any())).thenReturn(List.of(
                statItem("OVERVIEW", "重大危险源", 665, null, null, 1),
                statItem("OVERVIEW", "生产设施", 56, null, null, 2),
                statItem("OVERVIEW", "厂界", 56, null, null, 3),
                statItem("OVERVIEW", "封闭入口", 55, null, null, 4),
                statItem("OVERVIEW", "其他入口", 66, null, null, 5),
                statItem("OVERVIEW", "其它", 6, null, null, 6),
                statItem("EVENT", "区域入侵", 152, "#f0b429", null, 1)));
        when(majorHazardMapper.selectCount(any())).thenReturn(12L);
        // 维修工单：V88 起由 fac_tv_maintenance_order GROUP BY order_status 实时计数（取代字典值）。
        // 此处模拟 12 未接单 / 25 处理中 / 8 已超时（与 V88 种子口径一致）。
        List<FacTvMaintenanceOrder> orders = new ArrayList<>();
        for (int i = 0; i < 12; i++) orders.add(maintOrder("PENDING"));
        for (int i = 0; i < 25; i++) orders.add(maintOrder("PROCESSING"));
        for (int i = 0; i < 8; i++) orders.add(maintOrder("OVERTIME"));
        when(maintenanceOrderMapper.selectList(any())).thenReturn(orders);
        when(tvMonitorMapper.selectList(any())).thenReturn(List.of(
                monitorCat(true, "良好", "PRODUCTION"),
                monitorCat(true, "良好", "PRODUCTION"),
                monitorCat(true, "良好", "PRODUCTION"),
                monitorCat(true, "良好", "BOUNDARY"),
                monitorCat(true, "良好", "BOUNDARY"),
                monitorCat(true, "良好", "BOUNDARY"),
                monitorCat(true, "良好", "BOUNDARY"),
                monitorCat(false, "一般", "CLOSED_GATE"),
                monitorCat(true, "良好", "CLOSED_GATE"),
                monitorCat(true, "良好", "OTHER_GATE"),
                monitorCat(true, "良好", "OTHER"),
                monitorCat(true, "良好", null),
                monitorCat(true, "良好", null)));
        FacTvOperationStat stat = new FacTvOperationStat();
        stat.setEventTotal(110);
        when(operationStatMapper.selectList(any())).thenReturn(List.of(stat));

        TvOverview overview = service.overview();

        assertEquals(6, overview.getOverviewItems().size(), "OVERVIEW 含 6 个分类卡片");
        // 重大危险源：fac_major_hazard 实时计数（与 GET /hazards 同源），category=MAJOR_HAZARD，不再取手填 665
        var hazard = overview.getOverviewItems().get(0);
        assertEquals("重大危险源", hazard.getLabel());
        assertEquals(12, hazard.getValue());
        assertEquals("MAJOR_HAZARD", hazard.getCategory());
        // 其余 5 类：fac_tv_monitor.monitor_category 实时 GROUP BY 计数（V87 建立关联）
        java.util.Map<String, Integer> catValue = overview.getOverviewItems().stream()
                .collect(java.util.stream.Collectors.toMap(i -> i.getLabel(), i -> i.getValue()));
        assertEquals(3, catValue.get("生产设施"), "PRODUCTION 3 个点位");
        assertEquals(4, catValue.get("厂界"), "BOUNDARY 4 个点位");
        assertEquals(2, catValue.get("封闭入口"), "CLOSED_GATE 2 个点位");
        assertEquals(1, catValue.get("其他入口"), "OTHER_GATE 1 个点位");
        assertEquals(1, catValue.get("其它"), "OTHER 1 个点位");
        // 维保工单：fac_tv_maintenance_order GROUP BY order_status 实时计数（V88 取代字典值）
        var maint = overview.getMaintenanceOrders();
        assertEquals(3, maint.size(), "工单含 3 个状态卡片，按 PENDING/PROCESSING/OVERTIME 排序");
        assertEquals("未接单", maint.get(0).getLabel());
        assertEquals(12, maint.get(0).getValue());
        assertEquals("grey", maint.get(0).getTone());
        assertEquals("处理中", maint.get(1).getLabel());
        assertEquals(25, maint.get(1).getValue());
        assertEquals("已超时", maint.get(2).getLabel());
        assertEquals(8, maint.get(2).getValue());
        assertEquals("#f0b429", overview.getEventBreakdown().get(0).getColor());
        assertEquals(13, overview.getOperationStats().getTotal(), "运行统计仍由 fac_tv_monitor 明细聚合（含 2 个未分类）");
        assertEquals(1, overview.getOperationStats().getOffline());
        assertEquals(1, overview.getOperationStats().getFault());
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

    // ===================== 监控点位管理 CRUD（设备/防区管理，V87） =====================

    @Test
    void listMonitors_mapsSummaryWithZoneName() {
        when(zoneMapper.selectList(null)).thenReturn(List.of());
        FacTvMonitor m = new FacTvMonitor();
        m.setMonitorCode("ar-01");
        m.setMonitorName("高空AR-01");
        m.setOnline(true);
        m.setDepartment("安环部");
        m.setZoneCode("Z1");
        when(tvMonitorMapper.selectList(any())).thenReturn(List.of(m));

        List<TvMonitorSummary> list = service.listMonitors();

        assertEquals(1, list.size());
        assertEquals("ar-01", list.get(0).getCode());
        assertEquals("高空AR-01", list.get(0).getName());
        assertEquals("安环部", list.get(0).getDepartment());
        assertNull(list.get(0).getZoneName(), "zoneMapper 返回空时 zoneName 为 null（line 覆盖）");
    }

    @Test
    void createMonitor_persistsAndReturnsSummary() {
        when(zoneMapper.selectList(null)).thenReturn(List.of());
        when(tvMonitorMapper.selectOne(any())).thenReturn(null);
        when(tvMonitorMapper.insert(any(FacTvMonitor.class))).thenAnswer(inv -> {
            FacTvMonitor e = inv.getArgument(0);
            e.setId(10L);
            return 1;
        });

        TvMonitorUpsertRequest req = new TvMonitorUpsertRequest();
        req.setMonitorCode("ar-09");
        req.setMonitorName("新点位");
        req.setOnline(false);
        req.setDepartment("安保部");
        req.setZoneCode("Z9");
        req.setLocation("110.88,21.68");
        req.setHeight("24m");
        req.setAngle("56°");

        TvMonitorSummary s = service.createMonitor(req);
        assertEquals("ar-09", s.getCode());
        assertEquals("新点位", s.getName());
        assertFalse(s.getOnline());
        assertEquals("安保部", s.getDepartment());
        verify(tvMonitorMapper, times(1)).insert(any(FacTvMonitor.class));
    }

    @Test
    void createMonitor_blankCode_throws() {
        TvMonitorUpsertRequest req = new TvMonitorUpsertRequest();
        req.setMonitorCode("   ");
        assertThrows(BusinessException.class, () -> service.createMonitor(req));
    }

    @Test
    void createMonitor_duplicateCode_throws() {
        when(tvMonitorMapper.selectOne(any())).thenReturn(new FacTvMonitor());
        TvMonitorUpsertRequest req = new TvMonitorUpsertRequest();
        req.setMonitorCode("ar-01");
        assertThrows(BusinessException.class, () -> service.createMonitor(req));
    }

    @Test
    void updateMonitor_readModifyWrite() {
        when(zoneMapper.selectList(null)).thenReturn(List.of());
        FacTvMonitor existing = new FacTvMonitor();
        existing.setId(7L);
        existing.setMonitorCode("ar-07");
        existing.setMonitorName("旧名");
        existing.setOnline(true);
        when(tvMonitorMapper.selectOne(any())).thenReturn(existing);
        when(tvMonitorMapper.updateById(any())).thenReturn(1);

        TvMonitorUpsertRequest req = new TvMonitorUpsertRequest();
        req.setMonitorName("新名");
        req.setDepartment("运维部");
        req.setZoneCode("Z7");

        TvMonitorSummary s = service.updateMonitor("ar-07", req);
        assertEquals("ar-07", s.getCode());
        assertEquals("新名", s.getName());
        assertEquals("运维部", s.getDepartment());
        assertEquals("Z7", s.getZoneCode());
        verify(tvMonitorMapper, times(1)).updateById(any());
    }

    @Test
    void updateMonitor_notFound_throws() {
        when(tvMonitorMapper.selectOne(any())).thenReturn(null);
        TvMonitorUpsertRequest req = new TvMonitorUpsertRequest();
        req.setMonitorName("x");
        assertThrows(BusinessException.class, () -> service.updateMonitor("nope", req));
    }

    @Test
    void deleteMonitor_removesWhenExists() {
        FacTvMonitor existing = new FacTvMonitor();
        existing.setId(8L);
        existing.setMonitorCode("ar-08");
        when(tvMonitorMapper.selectOne(any())).thenReturn(existing);
        when(tvMonitorMapper.deleteById(8L)).thenReturn(1);

        service.deleteMonitor("ar-08");
        verify(tvMonitorMapper, times(1)).deleteById(8L);
    }

    @Test
    void deleteMonitor_notFound_throws() {
        when(tvMonitorMapper.selectOne(any())).thenReturn(null);
        assertThrows(BusinessException.class, () -> service.deleteMonitor("nope"));
    }

    // ===================== 维修工单真实台账（V88） =====================

    @Test
    void listMaintenanceOrders_mapsItemsAndStatusLabel() {
        FacTvMaintenanceOrder pending = new FacTvMaintenanceOrder();
        pending.setId(1L);
        pending.setOrderNo("WO-2026-0901");
        pending.setDeviceName("乙烯装置球机-01");
        pending.setStatus("PENDING");
        pending.setCreatedAt("2026-09-28 08:12:33");
        FacTvMaintenanceOrder processing = new FacTvMaintenanceOrder();
        processing.setId(2L);
        processing.setOrderNo("WO-2026-0801");
        processing.setDeviceName("乙烯装置球机-02");
        processing.setStatus("PROCESSING");
        processing.setAssignee("李伟");
        processing.setCreatedAt("2026-09-20 09:00:00");
        when(maintenanceOrderMapper.selectList(any())).thenReturn(List.of(pending, processing));

        // 纯 Mockito：mapper 被 mock，SQL 侧 status 过滤不生效，仅验证 Service 端的字段映射。
        var all = service.listMaintenanceOrders(null);
        assertEquals(2, all.size());
        assertEquals("未接单", all.get(0).getStatusLabel(), "statusLabel 由后端映射中文");
        assertNull(all.get(0).getAssignee(), "PENDING 无负责人→null（映射不走该行）");
        assertEquals("处理中", all.get(1).getStatusLabel());
        assertEquals("李伟", all.get(1).getAssignee());
        // status 透传到查询条件（构建 LambdaQueryWrapper），空串/blank 不拼 eq；此处只验证不抛 NPE。
        assertEquals(2, service.listMaintenanceOrders("PENDING").size());
    }

    @Test
    void listMaintenanceOrders_mapsStatusLabelAndNullableFields() {
        FacTvMaintenanceOrder o = new FacTvMaintenanceOrder();
        o.setId(3L);
        o.setOrderNo("WO-2026-0701");
        o.setDeviceName("乙烯装置枪机-01");
        o.setStatus("OVERTIME");
        o.setAssignee("刘洋");
        o.setCreatedAt("2026-09-10 09:00:00");
        o.setPlanFinishTime("2026-09-20 18:00:00");
        // actualFinishTime / handleDesc 为空，验证可空字段不抛 NPE
        when(maintenanceOrderMapper.selectList(any())).thenReturn(List.of(o));

        var items = service.listMaintenanceOrders("OVERTIME");
        assertEquals(1, items.size());
        assertEquals("已超时", items.get(0).getStatusLabel());
        assertNull(items.get(0).getActualFinishTime());
        assertNull(items.get(0).getHandleDesc());
    }

    @Test
    void getMaintenanceOrder_returnsNullWhenMissing() {
        when(maintenanceOrderMapper.selectById(99L)).thenReturn(null);
        assertNull(service.getMaintenanceOrder(99L));
    }

    @Test
    void getMaintenanceOrder_mapsItem() {
        FacTvMaintenanceOrder o = new FacTvMaintenanceOrder();
        o.setId(5L);
        o.setOrderNo("WO-2026-0901");
        o.setDeviceName("乙烯装置球机-01");
        o.setFaultDesc("画面持续模糊");
        o.setStatus("PENDING");
        o.setDepartment("储运车间");
        o.setZoneCode("YIXI");
        o.setCreatedAt("2026-09-28 08:12:33");
        when(maintenanceOrderMapper.selectById(5L)).thenReturn(o);

        var item = service.getMaintenanceOrder(5L);
        assertNotNull(item);
        assertEquals("WO-2026-0901", item.getOrderNo());
        assertEquals("未接单", item.getStatusLabel());
        assertEquals("储运车间", item.getDepartment());
        assertEquals("YIXI", item.getZoneCode());
    }

    @Test
    void monitorSnapshots_returnsPage() {
        FacTvSnapshot snap = new FacTvSnapshot();
        snap.setId(1L);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<FacTvSnapshot> pg =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 12);
        pg.setTotal(1L);
        pg.setRecords(List.of(snap));
        when(snapshotMapper.selectPage(any(), any())).thenReturn(pg);

        TvSnapshotPage out = service.monitorSnapshots("ar-01", 1, 12, null, null);
        assertEquals(1, out.getTotal());
        assertEquals(1, out.getList().size());
    }

    // ===================== 生产告警自动关联兜底（production-tv-snapshot-linkage） =====================

    @Test
    void autoRelate_writesBackOrphanSnapshotWithinWindowAndLocation() {
        // 防区主数据：Z1 -> 乙烯罐区（供 zoneNameMap 取 zoneName）
        SysZone zone = new SysZone();
        zone.setZoneCode("Z1");
        zone.setZoneName("乙烯罐区");
        when(zoneMapper.selectList(null)).thenReturn(List.of(zone));

        FacTvSnapshot orphan = new FacTvSnapshot();
        orphan.setId(7L);
        orphan.setMonitorName("高空AR-01");
        orphan.setZoneCode("Z1");
        orphan.setCaptureTime("2026-03-17 10:30:00");
        orphan.setAlarmId(null);
        when(snapshotMapper.selectList(any())).thenReturn(List.of(orphan));
        when(snapshotMapper.updateById(any())).thenReturn(1);

        int n = service.autoRelateSnapshotsForAlarm(42L, "PRODUCTION", "乙烯罐区", "2026-03-17 10:30:00");

        assertEquals(1, n, "窗口内 + 位置匹配 → 反写 1 条孤儿快照");
        ArgumentCaptor<FacTvSnapshot> cap = ArgumentCaptor.forClass(FacTvSnapshot.class);
        verify(snapshotMapper, times(1)).updateById(cap.capture());
        assertEquals(42L, cap.getValue().getAlarmId());
        assertEquals("PRODUCTION", cap.getValue().getAlarmType());
    }

    @Test
    void autoRelate_returnsZeroWhenLocationMismatch() {
        SysZone zone = new SysZone();
        zone.setZoneCode("Z1");
        zone.setZoneName("乙烯罐区");
        when(zoneMapper.selectList(null)).thenReturn(List.of(zone));

        FacTvSnapshot orphan = new FacTvSnapshot();
        orphan.setId(7L);
        orphan.setMonitorName("高空AR-01");
        orphan.setZoneCode("Z1");
        orphan.setCaptureTime("2026-03-17 10:30:00");
        when(snapshotMapper.selectList(any())).thenReturn(List.of(orphan));

        int n = service.autoRelateSnapshotsForAlarm(42L, "PRODUCTION", "码头区", "2026-03-17 10:30:00");

        assertEquals(0, n, "位置不符 → 不反写，返回 0（前端走空态，绝不编造）");
        verify(snapshotMapper, times(0)).updateById(any());
    }

    @Test
    void autoRelate_returnsZeroWhenLocationTooShort() {
        when(zoneMapper.selectList(null)).thenReturn(List.of());
        FacTvSnapshot orphan = new FacTvSnapshot();
        orphan.setId(7L);
        when(snapshotMapper.selectList(any())).thenReturn(List.of(orphan));

        int n = service.autoRelateSnapshotsForAlarm(42L, "PRODUCTION", "a", "2026-03-17 10:30:00");

        assertEquals(0, n, "location 关键字 <2 字符 → 直接返回 0（防误关联）");
        verify(snapshotMapper, times(0)).updateById(any());
    }
}
