package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.dto.FireBrigadeList;
import com.sinopec.mmsecurity.dto.FireBrigadeTeam;
import com.sinopec.mmsecurity.dto.RescueEquipmentItem;
import com.sinopec.mmsecurity.dto.RescueEquipmentList;
import com.sinopec.mmsecurity.dto.RescueBrigadeWriteRequest;
import com.sinopec.mmsecurity.dto.RescueEquipmentWriteRequest;
import com.sinopec.mmsecurity.dto.RescuePersonnelItem;
import com.sinopec.mmsecurity.dto.RescuePersonnelList;
import com.sinopec.mmsecurity.dto.RescuePersonnelWriteRequest;
import com.sinopec.mmsecurity.dto.RescueVehicleItem;
import com.sinopec.mmsecurity.dto.RescueVehicleList;
import com.sinopec.mmsecurity.dto.RescueVehicleWriteRequest;
import com.sinopec.mmsecurity.entity.FacBrigadeTeam;
import com.sinopec.mmsecurity.entity.FacRescueEquipment;
import com.sinopec.mmsecurity.entity.FacRescueOption;
import com.sinopec.mmsecurity.entity.FacRescuePersonnel;
import com.sinopec.mmsecurity.entity.FacRescueVehicle;
import com.sinopec.mmsecurity.entity.FacRescueVehicleCrew;
import com.sinopec.mmsecurity.entity.FacRescueVehicleEquipment;
import com.sinopec.mmsecurity.entity.FacRescueVehicleKv;
import com.sinopec.mmsecurity.mapper.FacBrigadeTeamMapper;
import com.sinopec.mmsecurity.mapper.FacRescueEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacRescueOptionMapper;
import com.sinopec.mmsecurity.mapper.FacRescuePersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleCrewMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleKvMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 应急救援资源服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class RescueResourceServiceTest {

    @Mock
    private FacRescueEquipmentMapper equipmentMapper;
    @Mock
    private FacRescuePersonnelMapper personnelMapper;
    @Mock
    private FacRescueOptionMapper optionMapper;
    @Mock
    private FacRescueVehicleMapper vehicleMapper;
    @Mock
    private FacRescueVehicleCrewMapper vehicleCrewMapper;
    @Mock
    private FacRescueVehicleEquipmentMapper vehicleEquipmentMapper;
    @Mock
    private FacRescueVehicleKvMapper vehicleKvMapper;
    @Mock
    private FacBrigadeTeamMapper brigadeTeamMapper;
    @Mock
    private DataScopeResolver dataScopeResolver;
    @Mock
    private EmergencyService emergencyService;

    @InjectMocks
    private RescueResourceService service;

    private static FacRescueOption option(String kind, String label, int sortNo) {
        FacRescueOption option = new FacRescueOption();
        option.setOptionKind(kind);
        option.setOptionLabel(label);
        option.setSortNo(sortNo);
        return option;
    }

    private static FacRescueEquipment equipment(Long id, String name, String squadron) {
        FacRescueEquipment row = new FacRescueEquipment();
        row.setId(id);
        row.setEquipName(name);
        row.setSquadron(squadron);
        row.setCategory("防护装备");
        row.setUnit("套");
        row.setQuantity(38);
        row.setLeaderName("张建");
        row.setLeaderPhone("17846865588");
        row.setStockQuantity(27);
        row.setEquipModel("RD400 型全面罩防毒面具");
        row.setProtectionType("综合防毒");
        row.setEquipmentStatus("正常可用");
        row.setScrapWarning("无");
        return row;
    }

    private static FacRescueVehicle vehicle(Long id, String plate, String type, String squadron) {
        FacRescueVehicle row = new FacRescueVehicle();
        row.setId(id);
        row.setPlate(plate);
        row.setVehicleType(type);
        row.setSquadron(squadron);
        row.setLeaderName("张建");
        row.setLeaderPhone("13677669527");
        row.setVehicleStatus("出动");
        row.setBusinessName(plate + " 泡沫车");
        row.setVehicleTypeFull("泡沫消防车");
        row.setInspectionStatus("正常有效");
        return row;
    }

    private static FacRescueVehicleKv kv(Long vehicleId, String kind, String label, String text, int sortNo) {
        FacRescueVehicleKv row = new FacRescueVehicleKv();
        row.setVehicleId(vehicleId);
        row.setKvKind(kind);
        row.setKvLabel(label);
        row.setValueText(text);
        row.setSortNo(sortNo);
        return row;
    }

    @Test
    void equipment_mapsItemsOptionsAndTotalSets() {
        when(equipmentMapper.selectList(any())).thenReturn(List.of(equipment(1L, "防毒面罩", "乙烯中队")));
        when(equipmentMapper.selectCount(any())).thenReturn(1L);
        when(optionMapper.selectList(any())).thenReturn(List.of(
                option("SQUADRON", "全部中队", 0), option("SQUADRON", "乙烯中队", 1)));

        RescueEquipmentList list = service.equipment(null);

        assertEquals(List.of("全部中队", "乙烯中队"), list.getSquadrons());
        assertEquals(1, list.getTotalSets(), "业务总量取台账真实条数，不再是写死的 375");
        RescueEquipmentItem item = list.getItems().get(0);
        assertEquals(1L, item.getId());
        assertEquals("防毒面罩", item.getName());
        assertEquals("乙烯中队", item.getSquadron());
        assertEquals(27, item.getStockQuantity());
        assertEquals("RD400 型全面罩防毒面具", item.getModel());
        assertEquals("正常可用", item.getEquipmentStatus());
    }

    @Test
    void equipment_allSquadronMeansNoFilter() {
        when(equipmentMapper.selectList(any())).thenReturn(List.of(equipment(1L, "防毒面罩", "乙烯中队")));
        when(equipmentMapper.selectCount(any())).thenReturn(1L);

        RescueEquipmentList list = service.equipment("全部中队");

        assertEquals(1, list.getItems().size());
        assertTrue(list.getSquadrons().isEmpty());
    }

    @Test
    void equipmentDetail_notFoundThrows404() {
        when(equipmentMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.equipmentDetail(99L));
        assertEquals(404, ex.getCode());
        assertEquals("救援装备不存在：id=99", ex.getMessage());
    }

    @Test
    void personnel_mapsRoleAndTotalCount() {
        FacRescuePersonnel row = new FacRescuePersonnel();
        row.setId(9L);
        row.setPersonName("周杰");
        row.setSquadron("乙烯中队");
        row.setPersonRole("副班长");
        when(personnelMapper.selectList(any())).thenReturn(List.of(row));
        when(personnelMapper.selectCount(any())).thenReturn(1L);
        when(optionMapper.selectList(any())).thenReturn(List.of(option("PERSONNEL_ROLE", "全部岗位", 0)));

        RescuePersonnelList list = service.personnel("乙烯中队", "全部岗位");

        assertEquals(List.of("全部岗位"), list.getRoles());
        assertEquals(1, list.getTotalCount(), "业务总量取台账真实条数，不再是写死的 375");
        RescuePersonnelItem item = list.getItems().get(0);
        assertEquals("周杰", item.getName());
        assertEquals("乙烯中队", item.getSquadron());
        assertEquals("副班长", item.getRole());
    }

    @Test
    void personnelDetail_notFoundThrows404() {
        when(personnelMapper.selectById(88L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.personnelDetail(88L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void vehicles_assemblesCrewOnboardConsumablesAndDispatch() {
        when(vehicleMapper.selectList(any())).thenReturn(List.of(vehicle(1L, "粤K-1231", "泡沫车", "乙烯中队")));
        FacRescueVehicleCrew crew = new FacRescueVehicleCrew();
        crew.setVehicleId(1L);
        crew.setMemberRole("车长");
        crew.setMemberName("张建");
        crew.setPhone("13677669527");
        crew.setDutyStatus("在岗");
        when(vehicleCrewMapper.selectList(any())).thenReturn(List.of(crew));
        FacRescueVehicleEquipment onboard = new FacRescueVehicleEquipment();
        onboard.setVehicleId(1L);
        onboard.setEquipName("空气呼吸器");
        onboard.setQuantity("4 套");
        onboard.setEquipModel("RHZKF6.8/30");
        when(vehicleEquipmentMapper.selectList(any())).thenReturn(List.of(onboard));
        when(vehicleKvMapper.selectList(any())).thenReturn(List.of(
                kv(1L, "CONSUMABLE", "抗溶泡沫液存量", "6.2m³", 1),
                kv(1L, "DISPATCH_SUMMARY", "本月出警次数", "3 次", 1)));

        RescueVehicleList list = service.vehicles(null, "泡沫车");

        RescueVehicleItem item = list.getItems().get(0);
        assertEquals("粤K-1231", item.getPlate());
        assertEquals("泡沫车", item.getType());
        assertEquals("出动", item.getStatus());
        assertEquals(1, item.getCrew().size());
        assertEquals("车长", item.getCrew().get(0).getRole());
        assertEquals(1, item.getOnboardEquipment().size());
        assertEquals("4 套", item.getOnboardEquipment().get(0).getQuantity());
        assertEquals(1, item.getConsumables().size());
        assertEquals("6.2m³", item.getConsumables().get(0).getValue());
        assertEquals(1, item.getDispatchSummary().size());
        assertEquals("本月出警次数", item.getDispatchSummary().get(0).getLabel());
        assertEquals("3 次", item.getDispatchSummary().get(0).getValue());
    }

    @Test
    void vehicles_emptyResultSkipsSubQueries() {
        when(vehicleMapper.selectList(any())).thenReturn(List.of());

        RescueVehicleList list = service.vehicles(null, null);

        assertTrue(list.getItems().isEmpty());
        verify(vehicleCrewMapper, never()).selectList(any());
        verify(vehicleKvMapper, never()).selectList(any());
    }

    @Test
    void vehicleDetail_notFoundThrows404() {
        when(vehicleMapper.selectById(77L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.vehicleDetail(77L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void brigades_assemblesChildrenFromFlatLedgerBySquadron() {
        FacBrigadeTeam team = new FacBrigadeTeam();
        team.setId(1L);
        team.setTeamName("乙烯中队");
        team.setArea("乙烯区");
        team.setMemberCount(12);
        team.setLeaderName("陈建");
        team.setLeaderPhone("13665898855");
        team.setLongitude(110.8908);
        team.setLatitude(21.6758);
        team.setRescuePersonnel(12);
        team.setRescueVehicles(4);
        when(brigadeTeamMapper.selectList(any())).thenReturn(List.of(team));
        // 队伍详情子集合统一来自扁平台账（按中队名归组，V62 起唯一真源）
        when(vehicleMapper.selectList(any())).thenReturn(List.of(vehicle(1L, "粤K-1231", "泡沫车", "乙烯中队")));
        FacRescuePersonnel person = new FacRescuePersonnel();
        person.setId(9L);
        person.setPersonName("周杰");
        person.setSquadron("乙烯中队");
        person.setPersonRole("副班长");
        person.setPersonGroup("指挥");
        person.setPhone("13700000009");
        person.setDutyStatus("在岗");
        when(personnelMapper.selectList(any())).thenReturn(List.of(person));
        when(equipmentMapper.selectList(any())).thenReturn(List.of(equipment(1L, "防毒面罩", "乙烯中队")));
        when(dataScopeResolver.resolveZones()).thenReturn(null);

        FireBrigadeList list = service.brigades("乙烯区");

        FireBrigadeTeam result = list.getItems().get(0);
        assertEquals("乙烯中队", result.getName());
        assertEquals(1, result.getVehicles().size());
        assertEquals("粤K-1231", result.getVehicles().get(0).getPlate());
        assertEquals("出动", result.getVehicles().get(0).getStatus());
        assertEquals(1, result.getPersonnel().size());
        assertEquals("指挥", result.getPersonnel().get(0).getGroup());
        assertEquals("在岗", result.getPersonnel().get(0).getDutyStatus());
        assertEquals(1, result.getEquipment().size());
        assertEquals("防护装备", result.getEquipment().get(0).getCategory());
        assertEquals(38, result.getEquipment().get(0).getCount());
        assertEquals("正常可用", result.getEquipment().get(0).getStatus());
    }

    @Test
    void brigadeDetail_notFoundThrows404() {
        when(brigadeTeamMapper.selectById(66L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.brigadeDetail(66L));
        assertEquals(404, ex.getCode());
    }

    /* ==================== 写侧：救援人员 ==================== */

    @Test
    void createPersonnel_persistsAndDerivesSortNoFromMax() {
        FacRescuePersonnel last = new FacRescuePersonnel();
        last.setSortNo(7);
        when(personnelMapper.selectOne(any())).thenReturn(last);

        RescuePersonnelWriteRequest req = new RescuePersonnelWriteRequest();
        req.setName("王强");
        req.setSquadron("炼油中队");
        req.setRole("指挥员");
        req.setPhone("13800000001");
        req.setDutyStatus("在岗");
        RescuePersonnelItem item = service.createPersonnel(req);

        ArgumentCaptor<FacRescuePersonnel> cap = ArgumentCaptor.forClass(FacRescuePersonnel.class);
        verify(personnelMapper).insert(cap.capture());
        assertEquals("王强", cap.getValue().getPersonName());
        assertEquals("指挥员", cap.getValue().getPersonRole());
        assertEquals(8, cap.getValue().getSortNo());
        assertEquals("王强", item.getName());
        assertEquals("13800000001", item.getPhone());
    }

    @Test
    void createPersonnel_blankNameThrowsParamInvalid() {
        RescuePersonnelWriteRequest req = new RescuePersonnelWriteRequest();
        req.setName("  ");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createPersonnel(req));
        assertTrue(ex.getMessage().contains("name"));
        verify(personnelMapper, never()).insert(any());
    }

    @Test
    void updatePersonnel_partialUpdateKeepsUntouchedFields() {
        FacRescuePersonnel existing = new FacRescuePersonnel();
        existing.setId(5L);
        existing.setPersonName("原名");
        existing.setSquadron("乙烯中队");
        existing.setPersonRole("战斗员");
        when(personnelMapper.selectById(5L)).thenReturn(existing);

        RescuePersonnelWriteRequest req = new RescuePersonnelWriteRequest();
        req.setDutyStatus("休整");
        RescuePersonnelItem item = service.updatePersonnel(5L, req);

        ArgumentCaptor<FacRescuePersonnel> cap = ArgumentCaptor.forClass(FacRescuePersonnel.class);
        verify(personnelMapper).updateById(cap.capture());
        assertEquals("休整", cap.getValue().getDutyStatus());
        // 未传字段保持原值（局部更新语义，不能被 null 覆盖）
        assertEquals("原名", cap.getValue().getPersonName());
        assertEquals("乙烯中队", cap.getValue().getSquadron());
        assertEquals("休整", item.getDutyStatus());
    }

    @Test
    void updatePersonnel_notFoundThrows404() {
        when(personnelMapper.selectById(404L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updatePersonnel(404L, new RescuePersonnelWriteRequest()));
        assertEquals(404, ex.getCode());
        verify(personnelMapper, never()).updateById(any());
    }

    @Test
    void deletePersonnel_removesRow() {
        when(personnelMapper.selectById(5L)).thenReturn(new FacRescuePersonnel());

        service.deletePersonnel(5L);

        verify(personnelMapper).deleteById(5L);
    }

    @Test
    void deletePersonnel_notFoundThrows404() {
        when(personnelMapper.selectById(404L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.deletePersonnel(404L));
        verify(personnelMapper, never()).deleteById(any());
    }

    /* ==================== 写侧：消防队伍 ==================== */

    @Test
    void createBrigade_persistsAndAssemblesChildren() {
        when(vehicleMapper.selectList(any())).thenReturn(List.of());
        when(personnelMapper.selectList(any())).thenReturn(List.of());
        when(equipmentMapper.selectList(any())).thenReturn(List.of());

        RescueBrigadeWriteRequest req = new RescueBrigadeWriteRequest();
        req.setName("化工特勤队");
        req.setArea("化工区");
        req.setLeaderName("张三");
        req.setLeaderPhone("13800000001");
        req.setMemberCount(32);
        FireBrigadeTeam team = service.createBrigade(req);

        ArgumentCaptor<FacBrigadeTeam> cap = ArgumentCaptor.forClass(FacBrigadeTeam.class);
        verify(brigadeTeamMapper).insert(cap.capture());
        assertEquals("化工特勤队", cap.getValue().getTeamName());
        assertEquals("化工区", cap.getValue().getArea());
        // 空表新增 → sort_no 从 1 起
        assertEquals(1, cap.getValue().getSortNo());
        assertEquals("化工特勤队", team.getName());
        assertEquals(0, team.getVehicles().size());
    }

    @Test
    void createBrigade_blankNameThrowsParamInvalid() {
        RescueBrigadeWriteRequest req = new RescueBrigadeWriteRequest();
        req.setName("");

        assertThrows(BusinessException.class, () -> service.createBrigade(req));
        verify(brigadeTeamMapper, never()).insert(any());
    }

    @Test
    void updateBrigade_partialUpdateKeepsUntouchedFields() {
        FacBrigadeTeam existing = new FacBrigadeTeam();
        existing.setId(3L);
        existing.setTeamName("原队名");
        existing.setArea("炼油区");
        when(brigadeTeamMapper.selectById(3L)).thenReturn(existing);
        when(vehicleMapper.selectList(any())).thenReturn(List.of());
        when(personnelMapper.selectList(any())).thenReturn(List.of());
        when(equipmentMapper.selectList(any())).thenReturn(List.of());

        RescueBrigadeWriteRequest req = new RescueBrigadeWriteRequest();
        req.setLeaderName("李队");
        FireBrigadeTeam team = service.updateBrigade(3L, req);

        ArgumentCaptor<FacBrigadeTeam> cap = ArgumentCaptor.forClass(FacBrigadeTeam.class);
        verify(brigadeTeamMapper).updateById(cap.capture());
        assertEquals("李队", cap.getValue().getLeaderName());
        assertEquals("原队名", cap.getValue().getTeamName());
        assertEquals("炼油区", cap.getValue().getArea());
        assertEquals("原队名", team.getName());
    }

    @Test
    void deleteBrigade_notFoundThrows404() {
        when(brigadeTeamMapper.selectById(404L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.deleteBrigade(404L));
        verify(brigadeTeamMapper, never()).deleteById(any());
    }

    /* ==================== 写侧：救援车辆 ==================== */

    @Test
    void createVehicle_plateIsRequiredAndSortNoStartsFromOne() {
        when(vehicleCrewMapper.selectList(any())).thenReturn(List.of());
        when(vehicleEquipmentMapper.selectList(any())).thenReturn(List.of());
        when(vehicleKvMapper.selectList(any())).thenReturn(List.of());

        RescueVehicleWriteRequest req = new RescueVehicleWriteRequest();
        req.setPlate("粤K12345");
        req.setType("泡沫消防车");
        req.setSquadron("化工特勤一中队");
        req.setLeaderName("张三");
        req.setLeaderPhone("13800000001");
        req.setStatus("出动");
        req.setBusinessName("粤K12345 泡沫车");
        req.setVehicleTypeFull("泡沫消防车");
        RescueVehicleItem item = service.createVehicle(req);

        ArgumentCaptor<FacRescueVehicle> cap = ArgumentCaptor.forClass(FacRescueVehicle.class);
        verify(vehicleMapper).insert(cap.capture());
        assertEquals("粤K12345", cap.getValue().getPlate());
        assertEquals("泡沫消防车", cap.getValue().getVehicleType());
        assertEquals(1, cap.getValue().getSortNo());
        assertEquals("粤K12345", item.getPlate());
    }

    @Test
    void createVehicle_blankPlateThrowsParamInvalid() {
        RescueVehicleWriteRequest req = new RescueVehicleWriteRequest();
        req.setType("泡沫消防车");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createVehicle(req));
        assertTrue(ex.getMessage().contains("plate"));
        verify(vehicleMapper, never()).insert(any());
    }

    @Test
    void updateVehicle_partialUpdateKeepsUntouchedFields() {
        FacRescueVehicle existing = new FacRescueVehicle();
        existing.setId(9L);
        existing.setPlate("粤K00000");
        existing.setVehicleType("水罐车");
        when(vehicleMapper.selectById(9L)).thenReturn(existing);
        when(vehicleCrewMapper.selectList(any())).thenReturn(List.of());
        when(vehicleEquipmentMapper.selectList(any())).thenReturn(List.of());
        when(vehicleKvMapper.selectList(any())).thenReturn(List.of());

        RescueVehicleWriteRequest req = new RescueVehicleWriteRequest();
        req.setStatus("维修");
        RescueVehicleItem item = service.updateVehicle(9L, req);

        ArgumentCaptor<FacRescueVehicle> cap = ArgumentCaptor.forClass(FacRescueVehicle.class);
        verify(vehicleMapper).updateById(cap.capture());
        assertEquals("维修", cap.getValue().getVehicleStatus());
        assertEquals("粤K00000", cap.getValue().getPlate());
        assertEquals("水罐车", cap.getValue().getVehicleType());
        assertEquals("维修", item.getStatus());
    }

    @Test
    void deleteVehicle_removesRow() {
        when(vehicleMapper.selectById(9L)).thenReturn(new FacRescueVehicle());

        service.deleteVehicle(9L);

        verify(vehicleMapper).deleteById(9L);
    }

    /* ==================== 写侧：救援装备 ==================== */

    @Test
    void createEquipment_persistsCategoryAndUnit() {
        RescueEquipmentWriteRequest req = new RescueEquipmentWriteRequest();
        req.setName("正压式空气呼吸器");
        req.setSquadron("炼油中队");
        req.setCategory("防护装备");
        req.setUnit("具");
        req.setQuantity(40);
        req.setLeaderName("张三");
        req.setLeaderPhone("13800000001");
        req.setModel("RHZKF6.8/30");
        RescueEquipmentItem item = service.createEquipment(req);

        ArgumentCaptor<FacRescueEquipment> cap = ArgumentCaptor.forClass(FacRescueEquipment.class);
        verify(equipmentMapper).insert(cap.capture());
        assertEquals("正压式空气呼吸器", cap.getValue().getEquipName());
        assertEquals("防护装备", cap.getValue().getCategory());
        assertEquals("具", cap.getValue().getUnit());
        assertEquals(1, cap.getValue().getSortNo());
        assertEquals("防护装备", item.getCategory());
        assertEquals("具", item.getUnit());
    }

    @Test
    void createEquipment_blankNameThrowsParamInvalid() {
        RescueEquipmentWriteRequest req = new RescueEquipmentWriteRequest();
        req.setCategory("防护装备");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createEquipment(req));
        assertTrue(ex.getMessage().contains("name"));
        verify(equipmentMapper, never()).insert(any());
    }

    @Test
    void updateEquipment_partialUpdateKeepsUntouchedFields() {
        FacRescueEquipment existing = equipment(11L, "原装备", "乙烯中队");
        when(equipmentMapper.selectById(11L)).thenReturn(existing);

        RescueEquipmentWriteRequest req = new RescueEquipmentWriteRequest();
        req.setStockQuantity(18);
        RescueEquipmentItem item = service.updateEquipment(11L, req);

        ArgumentCaptor<FacRescueEquipment> cap = ArgumentCaptor.forClass(FacRescueEquipment.class);
        verify(equipmentMapper).updateById(cap.capture());
        assertEquals(18, cap.getValue().getStockQuantity());
        assertEquals("原装备", cap.getValue().getEquipName());
        assertEquals("乙烯中队", cap.getValue().getSquadron());
        assertEquals(18, item.getStockQuantity());
    }

    @Test
    void deleteEquipment_notFoundThrows404() {
        when(equipmentMapper.selectById(404L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.deleteEquipment(404L));
        verify(equipmentMapper, never()).deleteById(any());
    }
}
