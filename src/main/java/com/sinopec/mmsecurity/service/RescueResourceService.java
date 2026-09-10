package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
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
import com.sinopec.mmsecurity.security.DataScopeHelper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应急救援资源域服务（救援装备 / 救援人员 / 救援车辆 / 消防队伍）。
 *
 * <p>数据来源为 V19 落地的 fac_rescue_* 与 fac_brigade_* 真实表，取代前端硬编码的
 * rescueEquipmentMock / rescuePersonnelMock / rescueVehicleMock / fireBrigadeMock 业务数据。
 * 列表与详情返回同一批对象（含子集合），前端详情可直接按 id 取。
 */
@Service
@RequiredArgsConstructor
public class RescueResourceService {

    private static final String KIND_SQUADRON = "SQUADRON";
    private static final String KIND_PERSONNEL_ROLE = "PERSONNEL_ROLE";
    private static final String KIND_VEHICLE_TYPE = "VEHICLE_TYPE";
    private static final String KIND_BRIGADE_AREA = "BRIGADE_AREA";
    private static final String KV_CONSUMABLE = "CONSUMABLE";
    private static final String KV_DISPATCH_SUMMARY = "DISPATCH_SUMMARY";
    /** mock 中的业务总量（列表仅分页展示条目，总量为独立常量）。 */
    private static final int EQUIPMENT_TOTAL_SETS = 375;
    private static final int PERSONNEL_TOTAL_COUNT = 375;

    private final FacRescueEquipmentMapper equipmentMapper;
    private final FacRescuePersonnelMapper personnelMapper;
    private final FacRescueOptionMapper optionMapper;
    private final FacRescueVehicleMapper vehicleMapper;
    private final FacRescueVehicleCrewMapper vehicleCrewMapper;
    private final FacRescueVehicleEquipmentMapper vehicleEquipmentMapper;
    private final FacRescueVehicleKvMapper vehicleKvMapper;
    private final FacBrigadeTeamMapper brigadeTeamMapper;
    private final FacBrigadeVehicleMapper brigadeVehicleMapper;
    private final FacBrigadePersonMapper brigadePersonMapper;
    private final FacBrigadeEquipmentMapper brigadeEquipmentMapper;
    private final DataScopeResolver dataScopeResolver;

    /** 救援装备列表：按中队过滤（null 或“全部中队”表示全部）。 */
    public RescueEquipmentList equipment(String squadron) {
        List<FacRescueEquipment> rows = equipmentMapper.selectList(
                new LambdaQueryWrapper<FacRescueEquipment>()
                        .eq(normalize(squadron) != null, FacRescueEquipment::getSquadron, normalize(squadron))
                        .orderByAsc(FacRescueEquipment::getId));
        RescueEquipmentList result = new RescueEquipmentList();
        result.setSquadrons(options(KIND_SQUADRON));
        result.setTotalSets(EQUIPMENT_TOTAL_SETS);
        result.setItems(rows.stream().map(this::toEquipmentItem).collect(Collectors.toList()));
        return result;
    }

    /** 救援装备详情。 */
    public RescueEquipmentItem equipmentDetail(Long id) {
        FacRescueEquipment row = equipmentMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "救援装备不存在：id=" + id);
        }
        return toEquipmentItem(row);
    }

    /** 救援人员列表：按中队 / 岗位过滤（null 或“全部*”表示全部）。 */
    public RescuePersonnelList personnel(String squadron, String role) {
        String squadronFilter = normalize(squadron);
        String roleFilter = normalize(role);
        List<FacRescuePersonnel> rows = personnelMapper.selectList(
                new LambdaQueryWrapper<FacRescuePersonnel>()
                        .eq(squadronFilter != null, FacRescuePersonnel::getSquadron, squadronFilter)
                        .eq(roleFilter != null, FacRescuePersonnel::getPersonRole, roleFilter)
                        .orderByAsc(FacRescuePersonnel::getId));
        RescuePersonnelList result = new RescuePersonnelList();
        result.setSquadrons(options(KIND_SQUADRON));
        result.setRoles(options(KIND_PERSONNEL_ROLE));
        result.setTotalCount(PERSONNEL_TOTAL_COUNT);
        result.setItems(rows.stream().map(this::toPersonnelItem).collect(Collectors.toList()));
        return result;
    }

    /** 救援人员详情。 */
    public RescuePersonnelItem personnelDetail(Long id) {
        FacRescuePersonnel row = personnelMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "救援人员不存在：id=" + id);
        }
        return toPersonnelItem(row);
    }

    /** 救援车辆列表：按中队 / 类型过滤，条目含乘员、随车装备、耗材与出动汇总。 */
    public RescueVehicleList vehicles(String squadron, String type) {
        String squadronFilter = normalize(squadron);
        String typeFilter = normalize(type);
        List<FacRescueVehicle> rows = vehicleMapper.selectList(
                new LambdaQueryWrapper<FacRescueVehicle>()
                        .eq(squadronFilter != null, FacRescueVehicle::getSquadron, squadronFilter)
                        .eq(typeFilter != null, FacRescueVehicle::getVehicleType, typeFilter)
                        .orderByAsc(FacRescueVehicle::getId));
        RescueVehicleList result = new RescueVehicleList();
        result.setSquadrons(options(KIND_SQUADRON));
        result.setTypes(options(KIND_VEHICLE_TYPE));
        result.setItems(toVehicleItems(rows));
        return result;
    }

    /** 救援车辆详情（含乘员 / 随车装备 / 耗材 / 出动汇总）。 */
    public RescueVehicleItem vehicleDetail(Long id) {
        FacRescueVehicle row = vehicleMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "救援车辆不存在：id=" + id);
        }
        return toVehicleItems(Collections.singletonList(row)).get(0);
    }

    /** 消防队伍列表：按区域过滤，条目含队伍车辆 / 人员 / 装备。 */
    public FireBrigadeList brigades(String area) {
        String areaFilter = normalize(area);
        // data_scope 行级 ABAC：解析当前登录用户可访问防区集合（null=不过滤/ALL，空集=1=0，非空=IN）
        Set<String> zones = dataScopeResolver.resolveZones();
        LambdaQueryWrapper<FacBrigadeTeam> qw = new LambdaQueryWrapper<>();
        if (areaFilter != null) {
            qw.eq(FacBrigadeTeam::getArea, areaFilter);
        }
        DataScopeHelper.apply(qw, FacBrigadeTeam::getArea, zones);
        List<FacBrigadeTeam> rows = brigadeTeamMapper.selectList(qw);
        FireBrigadeList result = new FireBrigadeList();
        result.setAreas(options(KIND_BRIGADE_AREA));
        result.setItems(toBrigadeTeams(rows));
        return result;
    }

    /** 消防队伍详情（含队伍车辆 / 人员 / 装备）。 */
    public FireBrigadeTeam brigadeDetail(Long id) {
        FacBrigadeTeam row = brigadeTeamMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消防队伍不存在：id=" + id);
        }
        return toBrigadeTeams(Collections.singletonList(row)).get(0);
    }

    // ------------------------------------------------------------------ 转换

    private RescueEquipmentItem toEquipmentItem(FacRescueEquipment row) {
        RescueEquipmentItem item = new RescueEquipmentItem();
        item.setId(row.getId());
        item.setName(row.getEquipName());
        item.setSquadron(row.getSquadron());
        item.setQuantity(row.getQuantity());
        item.setLeaderName(row.getLeaderName());
        item.setLeaderPhone(row.getLeaderPhone());
        item.setStockQuantity(row.getStockQuantity());
        item.setModel(row.getEquipModel());
        item.setProtectionType(row.getProtectionType());
        item.setFilterCanister(row.getFilterCanister());
        item.setMaxContinuousUse(row.getMaxContinuousUse());
        item.setStorageLocation(row.getStorageLocation());
        item.setPurchaseBatch(row.getPurchaseBatch());
        item.setFactoryValidityYears(row.getFactoryValidityYears());
        item.setRemainingValidity(row.getRemainingValidity());
        item.setLastInspectionDate(row.getLastInspectionDate());
        item.setNextMandatoryMaintenanceDate(row.getNextMandatoryMaintenanceDate());
        item.setEquipmentStatus(row.getEquipmentStatus());
        item.setScrapWarning(row.getScrapWarning());
        item.setIssueRegistration(row.getIssueRegistration());
        item.setSpareParts(row.getSpareParts());
        return item;
    }

    private RescuePersonnelItem toPersonnelItem(FacRescuePersonnel row) {
        RescuePersonnelItem item = new RescuePersonnelItem();
        item.setId(row.getId());
        item.setName(row.getPersonName());
        item.setSquadron(row.getSquadron());
        item.setRole(row.getPersonRole());
        return item;
    }

    /** 批量装配车辆及其子集合（一次查询子表后按 vehicle_id 归组，避免逐车查询）。 */
    private List<RescueVehicleItem> toVehicleItems(List<FacRescueVehicle> rows) {
        List<Long> ids = rows.stream().map(FacRescueVehicle::getId).collect(Collectors.toList());
        Map<Long, List<FacRescueVehicleCrew>> crewMap = new HashMap<>();
        Map<Long, List<FacRescueVehicleEquipment>> onboardMap = new HashMap<>();
        Map<Long, List<FacRescueVehicleKv>> kvMap = new HashMap<>();
        if (!ids.isEmpty()) {
            crewMap = vehicleCrewMapper.selectList(new LambdaQueryWrapper<FacRescueVehicleCrew>()
                            .in(FacRescueVehicleCrew::getVehicleId, ids)
                            .orderByAsc(FacRescueVehicleCrew::getSortNo))
                    .stream().collect(Collectors.groupingBy(FacRescueVehicleCrew::getVehicleId));
            onboardMap = vehicleEquipmentMapper.selectList(new LambdaQueryWrapper<FacRescueVehicleEquipment>()
                            .in(FacRescueVehicleEquipment::getVehicleId, ids)
                            .orderByAsc(FacRescueVehicleEquipment::getSortNo))
                    .stream().collect(Collectors.groupingBy(FacRescueVehicleEquipment::getVehicleId));
            kvMap = vehicleKvMapper.selectList(new LambdaQueryWrapper<FacRescueVehicleKv>()
                            .in(FacRescueVehicleKv::getVehicleId, ids)
                            .orderByAsc(FacRescueVehicleKv::getSortNo))
                    .stream().collect(Collectors.groupingBy(FacRescueVehicleKv::getVehicleId));
        }
        List<RescueVehicleItem> items = new ArrayList<>();
        for (FacRescueVehicle row : rows) {
            RescueVehicleItem item = new RescueVehicleItem();
            item.setId(row.getId());
            item.setPlate(row.getPlate());
            item.setType(row.getVehicleType());
            item.setSquadron(row.getSquadron());
            item.setLeaderName(row.getLeaderName());
            item.setLeaderPhone(row.getLeaderPhone());
            item.setStatus(row.getVehicleStatus());
            item.setBusinessName(row.getBusinessName());
            item.setVehicleTypeFull(row.getVehicleTypeFull());
            item.setParkingLocation(row.getParkingLocation());
            item.setChassisModel(row.getChassisModel());
            item.setManufactureDate(row.getManufactureDate());
            item.setInspectionExpiry(row.getInspectionExpiry());
            item.setFoamTankVolume(row.getFoamTankVolume());
            item.setWaterTankVolume(row.getWaterTankVolume());
            item.setMaxWaterFlow(row.getMaxWaterFlow());
            item.setFoamType(row.getFoamType());
            item.setLastMaintenanceDate(row.getLastMaintenanceDate());
            item.setNextMaintenanceDate(row.getNextMaintenanceDate());
            item.setTotalMileage(row.getTotalMileage());
            item.setFaultRecord(row.getFaultRecord());
            item.setInspectionStatus(row.getInspectionStatus());
            item.setCrew(crewMap.getOrDefault(row.getId(), Collections.emptyList()).stream()
                    .map(this::toCrewMember).collect(Collectors.toList()));
            item.setOnboardEquipment(onboardMap.getOrDefault(row.getId(), Collections.emptyList()).stream()
                    .map(this::toOnboardEquipment).collect(Collectors.toList()));
            List<FacRescueVehicleKv> kvs = kvMap.getOrDefault(row.getId(), Collections.emptyList());
            item.setConsumables(kvs.stream()
                    .filter(kv -> KV_CONSUMABLE.equals(kv.getKvKind()))
                    .map(this::toKvItem).collect(Collectors.toList()));
            item.setDispatchSummary(kvs.stream()
                    .filter(kv -> KV_DISPATCH_SUMMARY.equals(kv.getKvKind()))
                    .map(this::toKvItem).collect(Collectors.toList()));
            items.add(item);
        }
        return items;
    }

    private RescueVehicleCrewMember toCrewMember(FacRescueVehicleCrew row) {
        RescueVehicleCrewMember member = new RescueVehicleCrewMember();
        member.setRole(row.getMemberRole());
        member.setName(row.getMemberName());
        member.setPhone(row.getPhone());
        member.setCertificate(row.getCertificate());
        member.setDutyStatus(row.getDutyStatus());
        return member;
    }

    private RescueVehicleOnboardEquipment toOnboardEquipment(FacRescueVehicleEquipment row) {
        RescueVehicleOnboardEquipment equipment = new RescueVehicleOnboardEquipment();
        equipment.setName(row.getEquipName());
        equipment.setQuantity(row.getQuantity());
        equipment.setModel(row.getEquipModel());
        equipment.setNextCheckDate(row.getNextCheckDate());
        equipment.setEquipmentStatus(row.getEquipmentStatus());
        equipment.setStorageLocation(row.getStorageLocation());
        return equipment;
    }

    private KvItem toKvItem(FacRescueVehicleKv row) {
        KvItem item = new KvItem();
        item.setLabel(row.getKvLabel());
        item.setValue(row.getValueText());
        return item;
    }

    /** 批量装配消防队伍及其子集合（一次查询子表后按 team_id 归组）。 */
    private List<FireBrigadeTeam> toBrigadeTeams(List<FacBrigadeTeam> rows) {
        List<Long> ids = rows.stream().map(FacBrigadeTeam::getId).collect(Collectors.toList());
        Map<Long, List<FacBrigadeVehicle>> vehicleMap = new HashMap<>();
        Map<Long, List<FacBrigadePerson>> personMap = new HashMap<>();
        Map<Long, List<FacBrigadeEquipment>> equipmentMap = new HashMap<>();
        if (!ids.isEmpty()) {
            vehicleMap = brigadeVehicleMapper.selectList(new LambdaQueryWrapper<FacBrigadeVehicle>()
                            .in(FacBrigadeVehicle::getTeamId, ids)
                            .orderByAsc(FacBrigadeVehicle::getSortNo))
                    .stream().collect(Collectors.groupingBy(FacBrigadeVehicle::getTeamId));
            personMap = brigadePersonMapper.selectList(new LambdaQueryWrapper<FacBrigadePerson>()
                            .in(FacBrigadePerson::getTeamId, ids)
                            .orderByAsc(FacBrigadePerson::getSortNo))
                    .stream().collect(Collectors.groupingBy(FacBrigadePerson::getTeamId));
            equipmentMap = brigadeEquipmentMapper.selectList(new LambdaQueryWrapper<FacBrigadeEquipment>()
                            .in(FacBrigadeEquipment::getTeamId, ids)
                            .orderByAsc(FacBrigadeEquipment::getSortNo))
                    .stream().collect(Collectors.groupingBy(FacBrigadeEquipment::getTeamId));
        }
        List<FireBrigadeTeam> teams = new ArrayList<>();
        for (FacBrigadeTeam row : rows) {
            FireBrigadeTeam team = new FireBrigadeTeam();
            team.setId(row.getId());
            team.setName(row.getTeamName());
            team.setArea(row.getArea());
            team.setMemberCount(row.getMemberCount());
            team.setLeaderName(row.getLeaderName());
            team.setLeaderPhone(row.getLeaderPhone());
            team.setLocation(row.getLocation());
            team.setLongitude(row.getLongitude());
            team.setLatitude(row.getLatitude());
            team.setDescription(row.getDescription());
            team.setRescuePersonnel(row.getRescuePersonnel());
            team.setRescueVehicles(row.getRescueVehicles());
            team.setVehicles(vehicleMap.getOrDefault(row.getId(), Collections.emptyList()).stream()
                    .map(this::toBrigadeVehicle).collect(Collectors.toList()));
            team.setPersonnel(personMap.getOrDefault(row.getId(), Collections.emptyList()).stream()
                    .map(this::toBrigadePerson).collect(Collectors.toList()));
            team.setEquipment(equipmentMap.getOrDefault(row.getId(), Collections.emptyList()).stream()
                    .map(this::toBrigadeEquipment).collect(Collectors.toList()));
            teams.add(team);
        }
        return teams;
    }

    private FireBrigadeVehicle toBrigadeVehicle(FacBrigadeVehicle row) {
        FireBrigadeVehicle vehicle = new FireBrigadeVehicle();
        vehicle.setId(row.getId());
        vehicle.setPlate(row.getPlate());
        vehicle.setType(row.getVehicleType());
        vehicle.setStatus(row.getVehicleStatus());
        vehicle.setParkingLocation(row.getParkingLocation());
        return vehicle;
    }

    private FireBrigadePerson toBrigadePerson(FacBrigadePerson row) {
        FireBrigadePerson person = new FireBrigadePerson();
        person.setId(row.getId());
        person.setName(row.getPersonName());
        person.setRole(row.getPersonRole());
        person.setGroup(row.getPersonGroup());
        person.setPhone(row.getPhone());
        person.setDutyStatus(row.getDutyStatus());
        return person;
    }

    private FireBrigadeEquipment toBrigadeEquipment(FacBrigadeEquipment row) {
        FireBrigadeEquipment equipment = new FireBrigadeEquipment();
        equipment.setId(row.getId());
        equipment.setName(row.getEquipName());
        equipment.setCategory(row.getCategory());
        equipment.setCount(row.getItemCount());
        equipment.setUnit(row.getUnit());
        equipment.setStatus(row.getEquipStatus());
        equipment.setStorageLocation(row.getStorageLocation());
        return equipment;
    }

    /** 读取筛选项（含“全部*”首项）；无数据时返回空列表而非 null。 */
    private List<String> options(String kind) {
        return optionMapper.selectList(new LambdaQueryWrapper<FacRescueOption>()
                        .eq(FacRescueOption::getOptionKind, kind)
                        .orderByAsc(FacRescueOption::getSortNo))
                .stream().map(FacRescueOption::getOptionLabel).collect(Collectors.toList());
    }

    /** 归一化筛选参数：null / 空白 / “全部*”一律视为不过滤。 */
    private static String normalize(String filter) {
        if (filter == null || filter.isBlank() || filter.startsWith("全部")) {
            return null;
        }
        return filter.trim();
    }
}
