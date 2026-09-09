package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.dto.FireBrigadeList;
import com.sinopec.mmsecurity.dto.FireBrigadeTeam;
import com.sinopec.mmsecurity.dto.RescueEquipmentItem;
import com.sinopec.mmsecurity.dto.RescueEquipmentList;
import com.sinopec.mmsecurity.dto.RescuePersonnelItem;
import com.sinopec.mmsecurity.dto.RescuePersonnelList;
import com.sinopec.mmsecurity.dto.RescueVehicleItem;
import com.sinopec.mmsecurity.dto.RescueVehicleList;
import com.sinopec.mmsecurity.entity.FacBrigadeEquipment;
import com.sinopec.mmsecurity.entity.FacBrigadePerson;
import com.sinopec.mmsecurity.entity.FacBrigadeTeam;
import com.sinopec.mmsecurity.entity.FacBrigadeVehicle;
import com.sinopec.mmsecurity.entity.FacRescueEquipment;
import com.sinopec.mmsecurity.entity.FacRescueOption;
import com.sinopec.mmsecurity.entity.FacRescuePersonnel;
import com.sinopec.mmsecurity.entity.FacRescueVehicle;
import com.sinopec.mmsecurity.entity.FacRescueVehicleCrew;
import com.sinopec.mmsecurity.entity.FacRescueVehicleEquipment;
import com.sinopec.mmsecurity.entity.FacRescueVehicleKv;
import com.sinopec.mmsecurity.mapper.FacBrigadeEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacBrigadePersonMapper;
import com.sinopec.mmsecurity.mapper.FacBrigadeTeamMapper;
import com.sinopec.mmsecurity.mapper.FacBrigadeVehicleMapper;
import com.sinopec.mmsecurity.mapper.FacRescueEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacRescueOptionMapper;
import com.sinopec.mmsecurity.mapper.FacRescuePersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleCrewMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleKvMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private FacBrigadeVehicleMapper brigadeVehicleMapper;
    @Mock
    private FacBrigadePersonMapper brigadePersonMapper;
    @Mock
    private FacBrigadeEquipmentMapper brigadeEquipmentMapper;

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
        when(optionMapper.selectList(any())).thenReturn(List.of(
                option("SQUADRON", "全部中队", 0), option("SQUADRON", "乙烯中队", 1)));

        RescueEquipmentList list = service.equipment(null);

        assertEquals(List.of("全部中队", "乙烯中队"), list.getSquadrons());
        assertEquals(375, list.getTotalSets());
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
        when(optionMapper.selectList(any())).thenReturn(List.of(option("PERSONNEL_ROLE", "全部岗位", 0)));

        RescuePersonnelList list = service.personnel("乙烯中队", "全部岗位");

        assertEquals(List.of("全部岗位"), list.getRoles());
        assertEquals(375, list.getTotalCount());
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
    void brigades_assemblesVehiclesPersonnelAndEquipment() {
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
        FacBrigadeVehicle bVehicle = new FacBrigadeVehicle();
        bVehicle.setTeamId(1L);
        bVehicle.setPlate("粤K·X101");
        bVehicle.setVehicleType("水罐消防车");
        bVehicle.setVehicleStatus("待命");
        when(brigadeVehicleMapper.selectList(any())).thenReturn(List.of(bVehicle));
        FacBrigadePerson bPerson = new FacBrigadePerson();
        bPerson.setTeamId(1L);
        bPerson.setPersonName("陈建");
        bPerson.setPersonRole("队长");
        bPerson.setPersonGroup("指挥");
        bPerson.setDutyStatus("在岗");
        when(brigadePersonMapper.selectList(any())).thenReturn(List.of(bPerson));
        FacBrigadeEquipment bEquipment = new FacBrigadeEquipment();
        bEquipment.setTeamId(1L);
        bEquipment.setEquipName("空气呼吸器");
        bEquipment.setCategory("防护装备");
        bEquipment.setItemCount(24);
        bEquipment.setEquipStatus("完好");
        when(brigadeEquipmentMapper.selectList(any())).thenReturn(List.of(bEquipment));

        FireBrigadeList list = service.brigades("乙烯区");

        FireBrigadeTeam result = list.getItems().get(0);
        assertEquals("乙烯中队", result.getName());
        assertEquals(1, result.getVehicles().size());
        assertEquals("粤K·X101", result.getVehicles().get(0).getPlate());
        assertEquals("待命", result.getVehicles().get(0).getStatus());
        assertEquals(1, result.getPersonnel().size());
        assertEquals("指挥", result.getPersonnel().get(0).getGroup());
        assertEquals(1, result.getEquipment().size());
        assertEquals(24, result.getEquipment().get(0).getCount());
        assertEquals("完好", result.getEquipment().get(0).getStatus());
    }

    @Test
    void brigadeDetail_notFoundThrows404() {
        when(brigadeTeamMapper.selectById(66L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.brigadeDetail(66L));
        assertEquals(404, ex.getCode());
    }
}
