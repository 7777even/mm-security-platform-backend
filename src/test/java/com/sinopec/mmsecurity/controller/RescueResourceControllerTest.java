package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.FireBrigadeEquipment;
import com.sinopec.mmsecurity.dto.FireBrigadeList;
import com.sinopec.mmsecurity.dto.FireBrigadePerson;
import com.sinopec.mmsecurity.dto.FireBrigadeTeam;
import com.sinopec.mmsecurity.dto.FireBrigadeVehicle;
import com.sinopec.mmsecurity.dto.KvItem;
import com.sinopec.mmsecurity.dto.RescueEquipmentItem;
import com.sinopec.mmsecurity.dto.RescueEquipmentList;
import com.sinopec.mmsecurity.dto.RescuePersonnelItem;
import com.sinopec.mmsecurity.dto.RescuePersonnelList;
import com.sinopec.mmsecurity.dto.RescueVehicleCrewMember;
import com.sinopec.mmsecurity.dto.RescueVehicleItem;
import com.sinopec.mmsecurity.dto.RescueVehicleList;
import com.sinopec.mmsecurity.dto.RescueVehicleOnboardEquipment;
import com.sinopec.mmsecurity.service.RescueResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 应急救援资源接口校验（standalone MockMvc + 纯 Mockito，不起 Spring 上下文）。 */
@ExtendWith(MockitoExtension.class)
class RescueResourceControllerTest {

    @Mock
    private RescueResourceService service;

    @InjectMocks
    private RescueResourceController controller;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void equipment_returnsSquadronsTotalSetsAndItems() throws Exception {
        RescueEquipmentList list = new RescueEquipmentList();
        list.setSquadrons(List.of("全部中队", "乙烯中队"));
        list.setTotalSets(375);
        RescueEquipmentItem item = new RescueEquipmentItem();
        item.setId(1L);
        item.setName("防毒面罩");
        item.setSquadron("乙烯中队");
        item.setQuantity(38);
        item.setStockQuantity(27);
        item.setModel("RD400 型全面罩防毒面具");
        item.setEquipmentStatus("正常可用");
        list.setItems(List.of(item));
        when(service.equipment(isNull())).thenReturn(list);

        mvc().perform(get("/api/v1/rescue-resources/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.squadrons[0]").value("全部中队"))
                .andExpect(jsonPath("$.data.totalSets").value(375))
                .andExpect(jsonPath("$.data.items[0].name").value("防毒面罩"))
                .andExpect(jsonPath("$.data.items[0].stockQuantity").value(27))
                .andExpect(jsonPath("$.data.items[0].model").value("RD400 型全面罩防毒面具"));
    }

    @Test
    void equipment_passesSquadronFilterToService() throws Exception {
        RescueEquipmentList list = new RescueEquipmentList();
        list.setSquadrons(List.of("乙烯中队"));
        list.setTotalSets(375);
        list.setItems(List.of());
        when(service.equipment(eq("乙烯中队"))).thenReturn(list);

        mvc().perform(get("/api/v1/rescue-resources/equipment").param("squadron", "乙烯中队"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.squadrons[0]").value("乙烯中队"))
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void equipmentDetail_returnsSingleItem() throws Exception {
        RescueEquipmentItem item = new RescueEquipmentItem();
        item.setId(7L);
        item.setName("救生绳");
        item.setSquadron("特勤一中队");
        item.setEquipmentStatus("待检修");
        item.setScrapWarning("无");
        when(service.equipmentDetail(7L)).thenReturn(item);

        mvc().perform(get("/api/v1/rescue-resources/equipment/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.name").value("救生绳"))
                .andExpect(jsonPath("$.data.equipmentStatus").value("待检修"));
    }

    @Test
    void personnel_returnsOptionsTotalCountAndItems() throws Exception {
        RescuePersonnelList list = new RescuePersonnelList();
        list.setSquadrons(List.of("全部中队"));
        list.setRoles(List.of("全部岗位", "班长"));
        list.setTotalCount(375);
        RescuePersonnelItem item = new RescuePersonnelItem();
        item.setId(1L);
        item.setName("张建");
        item.setSquadron("乙烯中队");
        item.setRole("班长");
        list.setItems(List.of(item));
        when(service.personnel(isNull(), isNull())).thenReturn(list);

        mvc().perform(get("/api/v1/rescue-resources/personnel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[1]").value("班长"))
                .andExpect(jsonPath("$.data.totalCount").value(375))
                .andExpect(jsonPath("$.data.items[0].name").value("张建"))
                .andExpect(jsonPath("$.data.items[0].role").value("班长"));
    }

    @Test
    void personnelDetail_returnsSingleItem() throws Exception {
        RescuePersonnelItem item = new RescuePersonnelItem();
        item.setId(9L);
        item.setName("周杰");
        item.setSquadron("乙烯中队");
        item.setRole("副班长");
        when(service.personnelDetail(9L)).thenReturn(item);

        mvc().perform(get("/api/v1/rescue-resources/personnel/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.role").value("副班长"));
    }

    @Test
    void vehicles_returnsItemsWithSubCollections() throws Exception {
        RescueVehicleList list = new RescueVehicleList();
        list.setSquadrons(List.of("全部中队"));
        list.setTypes(List.of("全部类型", "泡沫车"));
        RescueVehicleItem item = new RescueVehicleItem();
        item.setId(1L);
        item.setPlate("粤K-1231");
        item.setType("泡沫车");
        item.setStatus("出动");
        RescueVehicleCrewMember crew = new RescueVehicleCrewMember();
        crew.setRole("车长");
        crew.setName("张建");
        crew.setPhone("13677669527");
        crew.setDutyStatus("在岗");
        item.setCrew(List.of(crew));
        RescueVehicleOnboardEquipment onboard = new RescueVehicleOnboardEquipment();
        onboard.setName("空气呼吸器");
        onboard.setQuantity("4 套");
        onboard.setModel("RHZKF6.8/30");
        item.setOnboardEquipment(List.of(onboard));
        KvItem consumable = new KvItem();
        consumable.setLabel("抗溶泡沫液存量");
        consumable.setValue("6.2m³");
        item.setConsumables(List.of(consumable));
        KvItem dispatch = new KvItem();
        dispatch.setLabel("本月出警次数");
        dispatch.setValue("3 次");
        item.setDispatchSummary(List.of(dispatch));
        list.setItems(List.of(item));
        when(service.vehicles(isNull(), isNull())).thenReturn(list);

        mvc().perform(get("/api/v1/rescue-resources/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.types[1]").value("泡沫车"))
                .andExpect(jsonPath("$.data.items[0].plate").value("粤K-1231"))
                .andExpect(jsonPath("$.data.items[0].crew[0].role").value("车长"))
                .andExpect(jsonPath("$.data.items[0].onboardEquipment[0].quantity").value("4 套"))
                .andExpect(jsonPath("$.data.items[0].consumables[0].value").value("6.2m³"))
                .andExpect(jsonPath("$.data.items[0].dispatchSummary[0].value").value("3 次"));
    }

    @Test
    void vehicleDetail_returnsSingleItem() throws Exception {
        RescueVehicleItem item = new RescueVehicleItem();
        item.setId(11L);
        item.setPlate("粤K-1211");
        item.setType("云梯消防车");
        item.setVehicleTypeFull("云梯消防车");
        item.setInspectionStatus("正常有效");
        item.setCrew(List.of());
        item.setOnboardEquipment(List.of());
        item.setConsumables(List.of());
        item.setDispatchSummary(List.of());
        when(service.vehicleDetail(11L)).thenReturn(item);

        mvc().perform(get("/api/v1/rescue-resources/vehicles/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plate").value("粤K-1211"))
                .andExpect(jsonPath("$.data.vehicleTypeFull").value("云梯消防车"));
    }

    @Test
    void brigades_returnsAreasAndTeamsWithSubCollections() throws Exception {
        FireBrigadeList list = new FireBrigadeList();
        list.setAreas(List.of("全部区域", "乙烯区"));
        FireBrigadeTeam team = new FireBrigadeTeam();
        team.setId(1L);
        team.setName("乙烯中队");
        team.setArea("乙烯区");
        team.setMemberCount(12);
        team.setLongitude(110.8908);
        team.setLatitude(21.6758);
        FireBrigadeVehicle vehicle = new FireBrigadeVehicle();
        vehicle.setId(1L);
        vehicle.setPlate("粤K·X101");
        vehicle.setType("水罐消防车");
        vehicle.setStatus("待命");
        team.setVehicles(List.of(vehicle));
        FireBrigadePerson person = new FireBrigadePerson();
        person.setId(1L);
        person.setName("陈建");
        person.setRole("队长");
        person.setGroup("指挥");
        person.setDutyStatus("在岗");
        team.setPersonnel(List.of(person));
        FireBrigadeEquipment equipment = new FireBrigadeEquipment();
        equipment.setId(1L);
        equipment.setName("空气呼吸器");
        equipment.setCategory("防护装备");
        equipment.setCount(24);
        equipment.setStatus("完好");
        team.setEquipment(List.of(equipment));
        list.setItems(List.of(team));
        when(service.brigades(isNull())).thenReturn(list);

        mvc().perform(get("/api/v1/rescue-resources/brigades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.areas[1]").value("乙烯区"))
                .andExpect(jsonPath("$.data.items[0].name").value("乙烯中队"))
                .andExpect(jsonPath("$.data.items[0].vehicles[0].plate").value("粤K·X101"))
                .andExpect(jsonPath("$.data.items[0].personnel[0].group").value("指挥"))
                .andExpect(jsonPath("$.data.items[0].equipment[0].count").value(24));
    }

    @Test
    void brigadeDetail_returnsSingleTeam() throws Exception {
        FireBrigadeTeam team = new FireBrigadeTeam();
        team.setId(3L);
        team.setName("罐区中队");
        team.setArea("罐区");
        team.setRescuePersonnel(16);
        team.setRescueVehicles(6);
        when(service.brigadeDetail(3L)).thenReturn(team);

        mvc().perform(get("/api/v1/rescue-resources/brigades/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("罐区中队"))
                .andExpect(jsonPath("$.data.rescueVehicles").value(6));
    }

    @Test
    void detail_notFound_returnsBusinessErrorCode() throws Exception {
        when(service.equipmentDetail(any())).thenThrow(
                new BusinessException(404, "救援装备不存在：id=999"));

        mvc().perform(get("/api/v1/rescue-resources/equipment/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("救援装备不存在：id=999"));
    }
}
