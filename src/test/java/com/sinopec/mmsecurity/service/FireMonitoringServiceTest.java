package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.FireEquipmentItem;
import com.sinopec.mmsecurity.dto.FireEquipmentStatus;
import com.sinopec.mmsecurity.dto.FirePatrolRecord;
import com.sinopec.mmsecurity.dto.RescueForceStat;
import com.sinopec.mmsecurity.dto.SpecialOperationStat;
import com.sinopec.mmsecurity.entity.FacFireFacilityMonitor;
import com.sinopec.mmsecurity.entity.FacFirePatrol;
import com.sinopec.mmsecurity.entity.FacFirePatrolItemDef;
import com.sinopec.mmsecurity.entity.FacFirePatrolItemResult;
import com.sinopec.mmsecurity.entity.FacSpecialOperationStat;
import com.sinopec.mmsecurity.entity.FacSpecialOperationTicket;
import com.sinopec.mmsecurity.mapper.FacBrigadeTeamMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacRescueEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacRescuePersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemDefMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemResultMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationStatMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationTicketMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 消防监控服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
class FireMonitoringServiceTest {

    private final FacSpecialOperationStatMapper specialOperationStatMapper =
            Mockito.mock(FacSpecialOperationStatMapper.class);
    private final FacSpecialOperationTicketMapper specialOperationTicketMapper =
            Mockito.mock(FacSpecialOperationTicketMapper.class);
    private final FacFireFacilityMonitorMapper fireFacilityMonitorMapper =
            Mockito.mock(FacFireFacilityMonitorMapper.class);
    private final FacBrigadeTeamMapper brigadeTeamMapper = Mockito.mock(FacBrigadeTeamMapper.class);
    private final FacRescuePersonnelMapper rescuePersonnelMapper = Mockito.mock(FacRescuePersonnelMapper.class);
    private final FacRescueEquipmentMapper rescueEquipmentMapper = Mockito.mock(FacRescueEquipmentMapper.class);
    private final FacRescueVehicleMapper rescueVehicleMapper = Mockito.mock(FacRescueVehicleMapper.class);
    private final FacFirePatrolMapper firePatrolMapper = Mockito.mock(FacFirePatrolMapper.class);
    private final FacFirePatrolItemDefMapper patrolItemDefMapper = Mockito.mock(FacFirePatrolItemDefMapper.class);
    private final FacFirePatrolItemResultMapper patrolItemResultMapper =
            Mockito.mock(FacFirePatrolItemResultMapper.class);

    private final FireMonitoringService service = new FireMonitoringService(
            specialOperationStatMapper, specialOperationTicketMapper, fireFacilityMonitorMapper,
            brigadeTeamMapper, rescuePersonnelMapper, rescueEquipmentMapper, rescueVehicleMapper,
            firePatrolMapper, patrolItemDefMapper, patrolItemResultMapper);

    @BeforeEach
    void resetCaches() {
        service.clearCaches();
    }

    @Test
    void rescueForces_countsFromFlatLedger() {
        Mockito.when(brigadeTeamMapper.selectCount(ArgumentMatchers.any())).thenReturn(8L);
        Mockito.when(rescuePersonnelMapper.selectCount(ArgumentMatchers.any())).thenReturn(52L);
        Mockito.when(rescueEquipmentMapper.selectCount(ArgumentMatchers.any())).thenReturn(35L);
        Mockito.when(rescueVehicleMapper.selectCount(ArgumentMatchers.any())).thenReturn(12L);

        List<RescueForceStat> out = service.rescueForces();
        assertEquals(4, out.size());
        assertEquals("消防队伍", out.get(0).getLabel());
        assertEquals(8, out.get(0).getValue());
        assertEquals("支", out.get(0).getUnit());
        assertEquals("squad", out.get(0).getIconType());
        assertEquals("救援人员", out.get(1).getLabel());
        assertEquals(52, out.get(1).getValue(), "人员取扁平资源台账计数（唯一真源，V62）");
        assertEquals("救援装备", out.get(2).getLabel());
        assertEquals(35, out.get(2).getValue(), "装备取扁平资源台账计数");
        assertEquals("救援车辆", out.get(3).getLabel());
        assertEquals(12, out.get(3).getValue(), "车辆取扁平资源台账计数");
        assertEquals("台", out.get(3).getUnit());
    }

    @Test
    void specialOperations_countsFromTicketDetail_keepsZeroForNoTicket() {
        FacSpecialOperationStat dict1 = new FacSpecialOperationStat();
        dict1.setId(1L);
        dict1.setLabel("动火作业");
        FacSpecialOperationStat dict2 = new FacSpecialOperationStat();
        dict2.setId(4L);
        dict2.setLabel("动土作业");
        Mockito.when(specialOperationStatMapper.selectList(ArgumentMatchers.any()))
                .thenReturn(List.of(dict1, dict2));
        Mockito.when(specialOperationTicketMapper.selectList(ArgumentMatchers.any()))
                .thenReturn(List.of(ticket("动火作业"), ticket("动火作业"), ticket("吊装作业")));

        List<SpecialOperationStat> out = service.specialOperations();
        assertEquals(2, out.size(), "类别与顺序沿用字典表");
        assertEquals("动火作业", out.get(0).getLabel());
        assertEquals(2, out.get(0).getCount(), "数量改为按明细票表 op_type 实时计数，不再取手填 stat_count");
        assertEquals(0, out.get(1).getCount(), "无票的类别保留 0，前端据此渲染灰色零值态");
    }

    private static FacSpecialOperationTicket ticket(String opType) {
        FacSpecialOperationTicket t = new FacSpecialOperationTicket();
        t.setOpType(opType);
        return t;
    }

    private static FacFireFacilityMonitor monitor(String type, int total, int online, int offline, int fault) {
        FacFireFacilityMonitor m = new FacFireFacilityMonitor();
        m.setFacilityType(type);
        m.setTotalCount(total);
        m.setOnlineCount(online);
        m.setOfflineCount(offline);
        m.setFaultCount(fault);
        return m;
    }

    @Test
    void equipmentStatus_returnsZerosWhenNoMonitorRows() {
        Mockito.when(fireFacilityMonitorMapper.selectList(ArgumentMatchers.any()))
                .thenReturn(Collections.emptyList());

        FireEquipmentStatus out = service.equipmentStatus();
        assertNotNull(out);
        assertEquals(0, out.getTotal());
        assertEquals(0, out.getOnlineRate());
    }

    @Test
    void equipmentStatus_sumsMonitorRowsAndComputesRates() {
        Mockito.when(fireFacilityMonitorMapper.selectList(ArgumentMatchers.any())).thenReturn(List.of(
                monitor("火灾自动报警系统", 128, 124, 4, 2),
                monitor("消防水源", 100, 90, 10, 5)));

        FireEquipmentStatus out = service.equipmentStatus();
        assertEquals(228, out.getTotal(), "total 为各类型 total_count 之和（与 /fire-facility/monitors 同源）");
        assertEquals(14, out.getOffline());
        assertEquals(7, out.getFault());
        assertEquals(94, out.getOnlineRate(), "(228-14)/228 = 93.9% → 94");
        assertEquals(97, out.getIntegrityRate(), "(228-7)/228 = 96.9% → 97");
    }

    @Test
    void patrols_fillsNormalForItemsWithoutAbnormalResult() {
        Mockito.when(patrolItemDefMapper.selectList(ArgumentMatchers.any())).thenReturn(defs("A1", "A2", "B1"));
        Mockito.when(patrolItemResultMapper.selectList(ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        Mockito.when(firePatrolMapper.selectList(ArgumentMatchers.any())).thenReturn(List.of(patrol(1L, "1#联合装置,中央控制室")));

        List<FirePatrolRecord> out = service.patrols();
        assertEquals(1, out.size());
        assertEquals(3, out.get(0).getCheckItems().size(), "标准表 3 项应全部补齐");
        assertTrue(out.get(0).getCheckItems().stream().allMatch(i -> "正常".equals(i.getResult())));
        assertEquals(Arrays.asList("1#联合装置", "中央控制室"), out.get(0).getLocations());
    }

    @Test
    void patrols_overridesAbnormalAndNotApplicablePerRecord() {
        Mockito.when(patrolItemDefMapper.selectList(ArgumentMatchers.any())).thenReturn(defs("B2", "C3"));
        FacFirePatrolItemResult abnormal = new FacFirePatrolItemResult();
        abnormal.setPatrolId(7L);
        abnormal.setItemCode("B2");
        abnormal.setCheckResult("异常");
        abnormal.setAbnormalDesc("疏散走道堆放施工材料");
        abnormal.setPhotoFile("patrol-photo-placeholder.png");
        FacFirePatrolItemResult notApplicable = new FacFirePatrolItemResult();
        notApplicable.setPatrolId(7L);
        notApplicable.setItemCode("C3");
        notApplicable.setCheckResult("不适用");
        Mockito.when(patrolItemResultMapper.selectList(ArgumentMatchers.any()))
                .thenReturn(List.of(abnormal, notApplicable));

        // 两条记录：7L 有异常覆盖，8L 应全部为「正常」（验证异常表按 patrolId 分组，不串数据）
        Mockito.when(firePatrolMapper.selectList(ArgumentMatchers.any()))
                .thenReturn(List.of(patrol(7L, "1#联合装置"), patrol(8L, "消防泵房")));

        List<FirePatrolRecord> out = service.patrols();
        assertEquals(2, out.size());
        FirePatrolRecord withAbnormal = out.stream().filter(r -> r.getId() == 7L).findFirst().orElseThrow();
        FirePatrolRecord clean = out.stream().filter(r -> r.getId() == 8L).findFirst().orElseThrow();

        assertEquals("异常", withAbnormal.getCheckItems().get(0).getResult());
        assertEquals("疏散走道堆放施工材料", withAbnormal.getCheckItems().get(0).getAbnormalDesc());
        assertEquals("patrol-photo-placeholder.png", withAbnormal.getCheckItems().get(0).getPhotoFile());
        assertEquals("不适用", withAbnormal.getCheckItems().get(1).getResult());

        assertTrue(clean.getCheckItems().stream().allMatch(i -> "正常".equals(i.getResult())),
                "异常结果不得跨记录串到其它巡查");
    }

    @Test
    void patrols_emptyLocationsYieldsEmptyList() {
        Mockito.when(patrolItemDefMapper.selectList(ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        Mockito.when(patrolItemResultMapper.selectList(ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        Mockito.when(firePatrolMapper.selectList(ArgumentMatchers.any())).thenReturn(List.of(patrol(9L, "  ")));

        List<FirePatrolRecord> out = service.patrols();
        assertTrue(out.get(0).getLocations().isEmpty(), "空白 locations 应输出空列表而非含空串元素");
    }

    private static List<FacFirePatrolItemDef> defs(String... codes) {
        return Arrays.stream(codes)
                .map(c -> {
                    FacFirePatrolItemDef d = new FacFirePatrolItemDef();
                    d.setItemCode(c);
                    d.setCategory("分类");
                    d.setContent("内容" + c);
                    return d;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private static FacFirePatrol patrol(Long id, String locations) {
        FacFirePatrol p = new FacFirePatrol();
        p.setId(id);
        p.setPatrolDate("2026-08-18");
        p.setShiftName("上午");
        p.setDutyPerson("李五");
        p.setPatrolCount("第1次");
        p.setLocations(locations);
        p.setCompleted(true);
        return p;
    }

    @Test
    void equipment_mapsMonitorRowsAsCategoryCounts() {
        Mockito.when(fireFacilityMonitorMapper.selectList(ArgumentMatchers.any())).thenReturn(List.of(
                monitor("火灾自动报警系统", 128, 124, 4, 2),
                monitor("消防水源", 100, 90, 10, 5)));

        List<FireEquipmentItem> out = service.equipment();
        assertEquals(2, out.size());
        assertEquals("火灾自动报警系统", out.get(0).getName());
        assertEquals(128, out.get(0).getCount(), "数量取自监测表 total_count（与 /fire-facility/monitors 同源）");
        assertEquals(100, out.get(1).getCount());
    }
}
