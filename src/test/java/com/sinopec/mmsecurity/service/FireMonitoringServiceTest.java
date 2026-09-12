package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.FireEquipmentItem;
import com.sinopec.mmsecurity.dto.FireEquipmentStatus;
import com.sinopec.mmsecurity.dto.FirePatrolRecord;
import com.sinopec.mmsecurity.dto.RescueForceStat;
import com.sinopec.mmsecurity.dto.SpecialOperationStat;
import com.sinopec.mmsecurity.entity.FacFireEquipmentCategory;
import com.sinopec.mmsecurity.entity.FacFireEquipmentStatus;
import com.sinopec.mmsecurity.entity.FacFirePatrol;
import com.sinopec.mmsecurity.entity.FacFirePatrolItemDef;
import com.sinopec.mmsecurity.entity.FacFirePatrolItemResult;
import com.sinopec.mmsecurity.entity.FacRescueForceStat;
import com.sinopec.mmsecurity.entity.FacSpecialOperationStat;
import com.sinopec.mmsecurity.mapper.FacFireEquipmentCategoryMapper;
import com.sinopec.mmsecurity.mapper.FacFireEquipmentStatusMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemDefMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemResultMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolMapper;
import com.sinopec.mmsecurity.mapper.FacRescueForceStatMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationStatMapper;
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

    private final FacRescueForceStatMapper rescueForceStatMapper = Mockito.mock(FacRescueForceStatMapper.class);
    private final FacSpecialOperationStatMapper specialOperationStatMapper =
            Mockito.mock(FacSpecialOperationStatMapper.class);
    private final FacFireEquipmentStatusMapper fireEquipmentStatusMapper =
            Mockito.mock(FacFireEquipmentStatusMapper.class);
    private final FacFirePatrolMapper firePatrolMapper = Mockito.mock(FacFirePatrolMapper.class);
    private final FacFirePatrolItemDefMapper patrolItemDefMapper = Mockito.mock(FacFirePatrolItemDefMapper.class);
    private final FacFirePatrolItemResultMapper patrolItemResultMapper =
            Mockito.mock(FacFirePatrolItemResultMapper.class);
    private final FacFireEquipmentCategoryMapper fireEquipmentCategoryMapper =
            Mockito.mock(FacFireEquipmentCategoryMapper.class);

    private final FireMonitoringService service = new FireMonitoringService(
            rescueForceStatMapper, specialOperationStatMapper, fireEquipmentStatusMapper,
            firePatrolMapper, patrolItemDefMapper, patrolItemResultMapper, fireEquipmentCategoryMapper);

    @BeforeEach
    void resetCaches() {
        service.clearCaches();
    }

    @Test
    void rescueForces_mapsStatCountToValue() {
        FacRescueForceStat row = new FacRescueForceStat();
        row.setLabel("救援人员");
        row.setStatCount(398);
        row.setUnit("人");
        row.setIconType("person");
        Mockito.when(rescueForceStatMapper.selectList(ArgumentMatchers.any())).thenReturn(List.of(row));

        List<RescueForceStat> out = service.rescueForces();
        assertEquals(1, out.size());
        assertEquals("救援人员", out.get(0).getLabel());
        assertEquals(398, out.get(0).getValue());
        assertEquals("人", out.get(0).getUnit());
        assertEquals("person", out.get(0).getIconType());
    }

    @Test
    void specialOperations_keepsZeroCount() {
        FacSpecialOperationStat row = new FacSpecialOperationStat();
        row.setId(4L);
        row.setLabel("动土作业");
        row.setStatCount(0);
        Mockito.when(specialOperationStatMapper.selectList(ArgumentMatchers.any())).thenReturn(List.of(row));

        List<SpecialOperationStat> out = service.specialOperations();
        assertEquals(0, out.get(0).getCount(), "零值作业需保留 0，前端据此渲染灰色零值态");
    }

    @Test
    void equipmentStatus_returnsZerosWhenTableEmpty() {
        Mockito.when(fireEquipmentStatusMapper.selectOne(ArgumentMatchers.any())).thenReturn(null);

        FireEquipmentStatus out = service.equipmentStatus();
        assertNotNull(out);
        assertEquals(0, out.getTotal());
        assertEquals(0, out.getOnlineRate());
    }

    @Test
    void equipmentStatus_mapsSnakeCaseColumns() {
        FacFireEquipmentStatus row = new FacFireEquipmentStatus();
        row.setTotalCnt(1233);
        row.setOfflineCnt(23);
        row.setFaultCnt(23);
        row.setIntegrityRate(98);
        row.setOnlineRate(98);
        Mockito.when(fireEquipmentStatusMapper.selectOne(ArgumentMatchers.any())).thenReturn(row);

        FireEquipmentStatus out = service.equipmentStatus();
        assertEquals(1233, out.getTotal());
        assertEquals(23, out.getOffline());
        assertEquals(23, out.getFault());
        assertEquals(98, out.getIntegrityRate());
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
    void equipment_mapsCategoryTable() {
        FacFireEquipmentCategory c1 = new FacFireEquipmentCategory();
        c1.setId(1L);
        c1.setCategoryName("火灾自动报警系统");
        c1.setEquipCount(665);
        FacFireEquipmentCategory c2 = new FacFireEquipmentCategory();
        c2.setId(2L);
        c2.setCategoryName("消防水源");
        c2.setEquipCount(665);
        Mockito.when(fireEquipmentCategoryMapper.selectList(ArgumentMatchers.any()))
                .thenReturn(List.of(c1, c2));

        List<FireEquipmentItem> out = service.equipment();
        assertEquals(2, out.size());
        assertEquals("火灾自动报警系统", out.get(0).getName());
        assertEquals(665, out.get(0).getCount());
        assertEquals(2L, out.get(1).getId());
    }
}
