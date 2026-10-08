package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.FireBrigadeEquipment;
import com.sinopec.mmsecurity.dto.FireBrigadeList;
import com.sinopec.mmsecurity.dto.FireBrigadePerson;
import com.sinopec.mmsecurity.dto.FireBrigadeTeam;
import com.sinopec.mmsecurity.dto.FireBrigadeVehicle;
import com.sinopec.mmsecurity.dto.KvItem;
import com.sinopec.mmsecurity.dto.RescueBrigadeWriteRequest;
import com.sinopec.mmsecurity.dto.RescueEquipmentItem;
import com.sinopec.mmsecurity.dto.RescueEquipmentList;
import com.sinopec.mmsecurity.dto.RescueEquipmentWriteRequest;
import com.sinopec.mmsecurity.dto.RescuePersonnelItem;
import com.sinopec.mmsecurity.dto.RescuePersonnelList;
import com.sinopec.mmsecurity.dto.RescuePersonnelWriteRequest;
import com.sinopec.mmsecurity.dto.RescueVehicleCrewMember;
import com.sinopec.mmsecurity.dto.RescueVehicleItem;
import com.sinopec.mmsecurity.dto.RescueVehicleList;
import com.sinopec.mmsecurity.dto.RescueVehicleOnboardEquipment;
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
import com.sinopec.mmsecurity.security.DataScopeHelper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
 * <p><b>救援力量唯一真源（V62 起）</b>：装备 / 人员 / 车辆一律取扁平资源台账
 * {@code fac_rescue_equipment / personnel / vehicle}；{@code fac_brigade_team} 仅保留为「中队主表」。
 * 「消防队伍详情」的队伍车辆 / 人员 / 装备改为按中队名从上述扁平表归组
 * （原 {@code fac_brigade_{vehicle,person,equipment}} 子表已退役）。
 * 由此队伍详情、管理端列表、消防大屏「消防救援力量」三者数字天然一致。
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

    /** 应急力量统计读穿缓存宿主（/emergency/strength 的 6 类计数取自本服务台账，写后须失效）。 */
    private final EmergencyService emergencyService;

    private final FacRescueEquipmentMapper equipmentMapper;
    private final FacRescuePersonnelMapper personnelMapper;
    private final FacRescueOptionMapper optionMapper;
    private final FacRescueVehicleMapper vehicleMapper;
    private final FacRescueVehicleCrewMapper vehicleCrewMapper;
    private final FacRescueVehicleEquipmentMapper vehicleEquipmentMapper;
    private final FacRescueVehicleKvMapper vehicleKvMapper;
    private final FacBrigadeTeamMapper brigadeTeamMapper;
    private final DataScopeResolver dataScopeResolver;

    /** 救援装备列表：按中队过滤（null 或“全部中队”表示全部）。 */
    public RescueEquipmentList equipment(String squadron) {
        List<FacRescueEquipment> rows = equipmentMapper.selectList(
                new LambdaQueryWrapper<FacRescueEquipment>()
                        .eq(normalize(squadron) != null, FacRescueEquipment::getSquadron, normalize(squadron))
                        .orderByAsc(FacRescueEquipment::getId));
        RescueEquipmentList result = new RescueEquipmentList();
        result.setSquadrons(options(KIND_SQUADRON));
        // 业务总量 = 台账真实条数（唯一真源），与消防大屏「救援装备」/ 应急面板卡片同源
        result.setTotalSets(Math.toIntExact(equipmentMapper.selectCount(null)));
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
        // 业务总量 = 台账真实条数（唯一真源），与消防大屏「救援人员」同源
        result.setTotalCount(Math.toIntExact(personnelMapper.selectCount(null)));
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

    /* ==================== 写侧：救援人员（应急专家） ==================== */

    /**
     * 新增救援人员（应急专家）。name / squadron / role 必填（对应表内 NOT NULL 列）；
     * sortNo 取当前最大值 +1。广播 {@code rescue.personnel}，管理端 / 大屏订阅方自动重拉。
     */
    @RealtimeSync(domain = "rescue.personnel")
    public RescuePersonnelItem createPersonnel(RescuePersonnelWriteRequest req) {
        if (req == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "请求体不能为空");
        }
        requireText(req.getName(), "人员姓名 name");
        requireText(req.getSquadron(), "所属中队 squadron");
        requireText(req.getRole(), "岗位 role");
        FacRescuePersonnel row = new FacRescuePersonnel();
        row.setPersonName(req.getName().trim());
        row.setSquadron(req.getSquadron());
        row.setPersonRole(req.getRole());
        row.setPersonGroup(req.getPersonGroup());
        row.setPhone(req.getPhone());
        row.setDutyStatus(req.getDutyStatus());
        row.setId(nextPersonnelId());
        row.setSortNo(nextPersonnelSortNo());
        personnelMapper.insert(row);
        emergencyService.invalidateStrengthCache();
        return toPersonnelItem(row);
    }

    /** 编辑救援人员：局部更新（字段为 null 表示不修改）。不存在抛 B3 NOT_FOUND。 */
    @RealtimeSync(domain = "rescue.personnel")
    public RescuePersonnelItem updatePersonnel(Long id, RescuePersonnelWriteRequest req) {
        FacRescuePersonnel row = personnelMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "救援人员不存在：id=" + id);
        }
        if (req.getName() != null) {
            row.setPersonName(req.getName());
        }
        if (req.getSquadron() != null) {
            row.setSquadron(req.getSquadron());
        }
        if (req.getRole() != null) {
            row.setPersonRole(req.getRole());
        }
        if (req.getPersonGroup() != null) {
            row.setPersonGroup(req.getPersonGroup());
        }
        if (req.getPhone() != null) {
            row.setPhone(req.getPhone());
        }
        if (req.getDutyStatus() != null) {
            row.setDutyStatus(req.getDutyStatus());
        }
        personnelMapper.updateById(row);
        emergencyService.invalidateStrengthCache();
        return toPersonnelItem(row);
    }

    /** 删除救援人员（物理删除：fac_rescue_personnel 无 deleted 列）。不存在抛 B3 NOT_FOUND。 */
    @RealtimeSync(domain = "rescue.personnel")
    public void deletePersonnel(Long id) {
        if (personnelMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "救援人员不存在：id=" + id);
        }
        personnelMapper.deleteById(id);
        emergencyService.invalidateStrengthCache();
    }

    /* ==================== 写侧：消防队伍（救援队伍） ==================== */

    /**
     * 新增消防队伍。name 必填；sortNo 取当前最大值 +1。
     * 经纬度可选——缺省时大屏地图不落点，前端按 null 跳过飞入。广播 {@code rescue.brigade}。
     */
    @RealtimeSync(domain = "rescue.brigade")
    public FireBrigadeTeam createBrigade(RescueBrigadeWriteRequest req) {
        if (req == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "请求体不能为空");
        }
        requireText(req.getName(), "队伍名称 name");
        requireText(req.getArea(), "所属区域 area");
        requireText(req.getLeaderName(), "负责人姓名 leaderName");
        requireText(req.getLeaderPhone(), "负责人电话 leaderPhone");
        FacBrigadeTeam row = new FacBrigadeTeam();
        row.setTeamName(req.getName().trim());
        row.setArea(req.getArea());
        row.setMemberCount(req.getMemberCount());
        row.setLeaderName(req.getLeaderName());
        row.setLeaderPhone(req.getLeaderPhone());
        row.setLocation(req.getLocation());
        row.setLongitude(req.getLongitude());
        row.setLatitude(req.getLatitude());
        row.setDescription(req.getDescription());
        row.setRescuePersonnel(req.getRescuePersonnel());
        row.setRescueVehicles(req.getRescueVehicles());
        row.setId(nextBrigadeId());
        row.setSortNo(nextBrigadeSortNo());
        brigadeTeamMapper.insert(row);
        emergencyService.invalidateStrengthCache();
        return toBrigadeTeams(Collections.singletonList(row)).get(0);
    }

    /** 编辑消防队伍：局部更新。不存在抛 B3 NOT_FOUND。广播 {@code rescue.brigade}。 */
    @RealtimeSync(domain = "rescue.brigade")
    public FireBrigadeTeam updateBrigade(Long id, RescueBrigadeWriteRequest req) {
        FacBrigadeTeam row = brigadeTeamMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消防队伍不存在：id=" + id);
        }
        if (req.getName() != null) {
            row.setTeamName(req.getName());
        }
        if (req.getArea() != null) {
            row.setArea(req.getArea());
        }
        if (req.getMemberCount() != null) {
            row.setMemberCount(req.getMemberCount());
        }
        if (req.getLeaderName() != null) {
            row.setLeaderName(req.getLeaderName());
        }
        if (req.getLeaderPhone() != null) {
            row.setLeaderPhone(req.getLeaderPhone());
        }
        if (req.getLocation() != null) {
            row.setLocation(req.getLocation());
        }
        if (req.getLongitude() != null) {
            row.setLongitude(req.getLongitude());
        }
        if (req.getLatitude() != null) {
            row.setLatitude(req.getLatitude());
        }
        if (req.getDescription() != null) {
            row.setDescription(req.getDescription());
        }
        if (req.getRescuePersonnel() != null) {
            row.setRescuePersonnel(req.getRescuePersonnel());
        }
        if (req.getRescueVehicles() != null) {
            row.setRescueVehicles(req.getRescueVehicles());
        }
        brigadeTeamMapper.updateById(row);
        emergencyService.invalidateStrengthCache();
        return toBrigadeTeams(Collections.singletonList(row)).get(0);
    }

    /** 删除消防队伍（物理删除）。不存在抛 B3 NOT_FOUND。广播 {@code rescue.brigade}。 */
    @RealtimeSync(domain = "rescue.brigade")
    public void deleteBrigade(Long id) {
        if (brigadeTeamMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消防队伍不存在：id=" + id);
        }
        brigadeTeamMapper.deleteById(id);
        emergencyService.invalidateStrengthCache();
    }

    /* ==================== 写侧：救援车辆 ==================== */

    /**
     * 新增救援车辆。plate 必填（车牌是台账唯一业务标识）；sortNo 取当前最大值 +1。
     * 只写车辆本体——乘员 / 随车装备 / 耗材 / 出动汇总分属子表，台账保存不改写子表行。
     * 广播 {@code rescue.vehicle}。
     */
    @RealtimeSync(domain = "rescue.vehicle")
    public RescueVehicleItem createVehicle(RescueVehicleWriteRequest req) {
        if (req == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "请求体不能为空");
        }
        requireText(req.getPlate(), "车牌号 plate");
        requireText(req.getType(), "车辆类型 type");
        requireText(req.getSquadron(), "所属中队 squadron");
        requireText(req.getLeaderName(), "车长姓名 leaderName");
        requireText(req.getLeaderPhone(), "车长电话 leaderPhone");
        requireText(req.getStatus(), "车辆状态 status");
        requireText(req.getBusinessName(), "业务名称 businessName");
        requireText(req.getVehicleTypeFull(), "车辆类型全称 vehicleTypeFull");
        FacRescueVehicle row = new FacRescueVehicle();
        row.setPlate(req.getPlate().trim());
        applyVehicleFields(row, req);
        row.setId(nextVehicleId());
        row.setSortNo(nextVehicleSortNo());
        vehicleMapper.insert(row);
        emergencyService.invalidateStrengthCache();
        return toVehicleItems(Collections.singletonList(row)).get(0);
    }

    /** 编辑救援车辆：局部更新（字段为 null 表示不修改）。不存在抛 B3 NOT_FOUND。 */
    @RealtimeSync(domain = "rescue.vehicle")
    public RescueVehicleItem updateVehicle(Long id, RescueVehicleWriteRequest req) {
        FacRescueVehicle row = vehicleMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "救援车辆不存在：id=" + id);
        }
        applyVehicleFields(row, req);
        vehicleMapper.updateById(row);
        emergencyService.invalidateStrengthCache();
        return toVehicleItems(Collections.singletonList(row)).get(0);
    }

    /** 删除救援车辆（物理删除）。不存在抛 B3 NOT_FOUND。广播 {@code rescue.vehicle}。 */
    @RealtimeSync(domain = "rescue.vehicle")
    public void deleteVehicle(Long id) {
        if (vehicleMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "救援车辆不存在：id=" + id);
        }
        vehicleMapper.deleteById(id);
        emergencyService.invalidateStrengthCache();
    }

    /** 车辆字段局部赋值（create / update 共用；null 表示不覆盖，plate 由各方法单独处理）。 */
    private static void applyVehicleFields(FacRescueVehicle row, RescueVehicleWriteRequest req) {
        if (req.getType() != null) {
            row.setVehicleType(req.getType());
        }
        if (req.getSquadron() != null) {
            row.setSquadron(req.getSquadron());
        }
        if (req.getLeaderName() != null) {
            row.setLeaderName(req.getLeaderName());
        }
        if (req.getLeaderPhone() != null) {
            row.setLeaderPhone(req.getLeaderPhone());
        }
        if (req.getStatus() != null) {
            row.setVehicleStatus(req.getStatus());
        }
        if (req.getBusinessName() != null) {
            row.setBusinessName(req.getBusinessName());
        }
        if (req.getVehicleTypeFull() != null) {
            row.setVehicleTypeFull(req.getVehicleTypeFull());
        }
        if (req.getParkingLocation() != null) {
            row.setParkingLocation(req.getParkingLocation());
        }
        if (req.getChassisModel() != null) {
            row.setChassisModel(req.getChassisModel());
        }
        if (req.getManufactureDate() != null) {
            row.setManufactureDate(req.getManufactureDate());
        }
        if (req.getInspectionExpiry() != null) {
            row.setInspectionExpiry(req.getInspectionExpiry());
        }
        if (req.getFoamTankVolume() != null) {
            row.setFoamTankVolume(req.getFoamTankVolume());
        }
        if (req.getWaterTankVolume() != null) {
            row.setWaterTankVolume(req.getWaterTankVolume());
        }
        if (req.getMaxWaterFlow() != null) {
            row.setMaxWaterFlow(req.getMaxWaterFlow());
        }
        if (req.getFoamType() != null) {
            row.setFoamType(req.getFoamType());
        }
        if (req.getLastMaintenanceDate() != null) {
            row.setLastMaintenanceDate(req.getLastMaintenanceDate());
        }
        if (req.getNextMaintenanceDate() != null) {
            row.setNextMaintenanceDate(req.getNextMaintenanceDate());
        }
        if (req.getTotalMileage() != null) {
            row.setTotalMileage(req.getTotalMileage());
        }
        if (req.getFaultRecord() != null) {
            row.setFaultRecord(req.getFaultRecord());
        }
        if (req.getInspectionStatus() != null) {
            row.setInspectionStatus(req.getInspectionStatus());
        }
    }

    /* ==================== 写侧：救援装备（应急物资） ==================== */

    /**
     * 新增救援装备。name 必填；sortNo 取当前最大值 +1。广播 {@code rescue.equipment}。
     */
    @RealtimeSync(domain = "rescue.equipment")
    public RescueEquipmentItem createEquipment(RescueEquipmentWriteRequest req) {
        if (req == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "请求体不能为空");
        }
        requireText(req.getName(), "装备名称 name");
        requireText(req.getSquadron(), "所属中队 squadron");
        requireText(req.getLeaderName(), "责任人姓名 leaderName");
        requireText(req.getLeaderPhone(), "责任人电话 leaderPhone");
        requireText(req.getModel(), "规格型号 model");
        FacRescueEquipment row = new FacRescueEquipment();
        row.setEquipName(req.getName().trim());
        applyEquipmentFields(row, req);
        row.setId(nextEquipmentId());
        row.setSortNo(nextEquipmentSortNo());
        equipmentMapper.insert(row);
        emergencyService.invalidateStrengthCache();
        return toEquipmentItem(row);
    }

    /** 编辑救援装备：局部更新（字段为 null 表示不修改）。不存在抛 B3 NOT_FOUND。 */
    @RealtimeSync(domain = "rescue.equipment")
    public RescueEquipmentItem updateEquipment(Long id, RescueEquipmentWriteRequest req) {
        FacRescueEquipment row = equipmentMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "救援装备不存在：id=" + id);
        }
        applyEquipmentFields(row, req);
        equipmentMapper.updateById(row);
        emergencyService.invalidateStrengthCache();
        return toEquipmentItem(row);
    }

    /** 删除救援装备（物理删除）。不存在抛 B3 NOT_FOUND。广播 {@code rescue.equipment}。 */
    @RealtimeSync(domain = "rescue.equipment")
    public void deleteEquipment(Long id) {
        if (equipmentMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "救援装备不存在：id=" + id);
        }
        equipmentMapper.deleteById(id);
        emergencyService.invalidateStrengthCache();
    }

    /** 装备字段局部赋值（create / update 共用；null 表示不覆盖，name 由各方法单独处理）。 */
    private static void applyEquipmentFields(FacRescueEquipment row, RescueEquipmentWriteRequest req) {
        if (req.getSquadron() != null) {
            row.setSquadron(req.getSquadron());
        }
        if (req.getCategory() != null) {
            row.setCategory(req.getCategory());
        }
        if (req.getUnit() != null) {
            row.setUnit(req.getUnit());
        }
        if (req.getQuantity() != null) {
            row.setQuantity(req.getQuantity());
        }
        if (req.getLeaderName() != null) {
            row.setLeaderName(req.getLeaderName());
        }
        if (req.getLeaderPhone() != null) {
            row.setLeaderPhone(req.getLeaderPhone());
        }
        if (req.getStockQuantity() != null) {
            row.setStockQuantity(req.getStockQuantity());
        }
        if (req.getModel() != null) {
            row.setEquipModel(req.getModel());
        }
        if (req.getProtectionType() != null) {
            row.setProtectionType(req.getProtectionType());
        }
        if (req.getFilterCanister() != null) {
            row.setFilterCanister(req.getFilterCanister());
        }
        if (req.getMaxContinuousUse() != null) {
            row.setMaxContinuousUse(req.getMaxContinuousUse());
        }
        if (req.getStorageLocation() != null) {
            row.setStorageLocation(req.getStorageLocation());
        }
        if (req.getPurchaseBatch() != null) {
            row.setPurchaseBatch(req.getPurchaseBatch());
        }
        if (req.getFactoryValidityYears() != null) {
            row.setFactoryValidityYears(req.getFactoryValidityYears());
        }
        if (req.getRemainingValidity() != null) {
            row.setRemainingValidity(req.getRemainingValidity());
        }
        if (req.getLastInspectionDate() != null) {
            row.setLastInspectionDate(req.getLastInspectionDate());
        }
        if (req.getNextMandatoryMaintenanceDate() != null) {
            row.setNextMandatoryMaintenanceDate(req.getNextMandatoryMaintenanceDate());
        }
        if (req.getEquipmentStatus() != null) {
            row.setEquipmentStatus(req.getEquipmentStatus());
        }
        if (req.getScrapWarning() != null) {
            row.setScrapWarning(req.getScrapWarning());
        }
        if (req.getIssueRegistration() != null) {
            row.setIssueRegistration(req.getIssueRegistration());
        }
        if (req.getSpareParts() != null) {
            row.setSpareParts(req.getSpareParts());
        }
    }

    /**
     * 校验必填文本：与表内 NOT NULL 列一一对应。
     *
     * <p>不靠数据库兜底——DB 抛的是 {@code DataIntegrityViolationException}，
     * 前端只能看到「数据冲突：请检查唯一键或必填字段」，无从定位是哪个字段没填。</p>
     */
    private static void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, field + " 不能为空");
        }
    }

    /**
     * 新增行主键：max(id) + 1。
     *
     * <p>四张表的种子数据都<b>显式指定过 id</b>，而自增序列不会随之推进
     * （H2 / PG / 达梦一致），交给库分配会让新增行撞主键 → 恒定 409。
     * 统一走 {@link LedgerIdSupport#nextId}，原因与取舍详见该类的类注释。</p>
     */
    private long nextPersonnelId() {
        return LedgerIdSupport.nextId(personnelMapper, FacRescuePersonnel::getId, FacRescuePersonnel::getId);
    }

    private long nextBrigadeId() {
        return LedgerIdSupport.nextId(brigadeTeamMapper, FacBrigadeTeam::getId, FacBrigadeTeam::getId);
    }

    private long nextVehicleId() {
        return LedgerIdSupport.nextId(vehicleMapper, FacRescueVehicle::getId, FacRescueVehicle::getId);
    }

    private long nextEquipmentId() {
        return LedgerIdSupport.nextId(equipmentMapper, FacRescueEquipment::getId, FacRescueEquipment::getId);
    }

    /** 新增排序号：当前最大 sort_no + 1（空表从 1 起），保证新行排在台账末尾。 */
    private int nextPersonnelSortNo() {
        return LedgerIdSupport.nextSortNo(
                personnelMapper, FacRescuePersonnel::getSortNo, FacRescuePersonnel::getSortNo);
    }

    private int nextBrigadeSortNo() {
        return LedgerIdSupport.nextSortNo(
                brigadeTeamMapper, FacBrigadeTeam::getSortNo, FacBrigadeTeam::getSortNo);
    }

    private int nextVehicleSortNo() {
        return LedgerIdSupport.nextSortNo(
                vehicleMapper, FacRescueVehicle::getSortNo, FacRescueVehicle::getSortNo);
    }

    private int nextEquipmentSortNo() {
        return LedgerIdSupport.nextSortNo(
                equipmentMapper, FacRescueEquipment::getSortNo, FacRescueEquipment::getSortNo);
    }

    private RescueEquipmentItem toEquipmentItem(FacRescueEquipment row) {
        RescueEquipmentItem item = new RescueEquipmentItem();
        item.setId(row.getId());
        item.setName(row.getEquipName());
        item.setSquadron(row.getSquadron());
        item.setCategory(row.getCategory());
        item.setUnit(row.getUnit());
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
        item.setPersonGroup(row.getPersonGroup());
        item.setPhone(row.getPhone());
        item.setDutyStatus(row.getDutyStatus());
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

    /**
     * 批量装配消防队伍及其子集合：队伍车辆 / 人员 / 装备一律由「扁平资源台账」按中队名归组
     * （唯一真源，V62 起），一次查询后按 squadron 归组，避免逐队查询。
     */
    private List<FireBrigadeTeam> toBrigadeTeams(List<FacBrigadeTeam> rows) {
        List<String> names = rows.stream().map(FacBrigadeTeam::getTeamName).collect(Collectors.toList());
        Map<String, List<FacRescueVehicle>> vehicleMap = new HashMap<>();
        Map<String, List<FacRescuePersonnel>> personMap = new HashMap<>();
        Map<String, List<FacRescueEquipment>> equipmentMap = new HashMap<>();
        if (!names.isEmpty()) {
            vehicleMap = vehicleMapper.selectList(new LambdaQueryWrapper<FacRescueVehicle>()
                            .in(FacRescueVehicle::getSquadron, names)
                            .orderByAsc(FacRescueVehicle::getId))
                    .stream().collect(Collectors.groupingBy(FacRescueVehicle::getSquadron));
            personMap = personnelMapper.selectList(new LambdaQueryWrapper<FacRescuePersonnel>()
                            .in(FacRescuePersonnel::getSquadron, names)
                            .orderByAsc(FacRescuePersonnel::getId))
                    .stream().collect(Collectors.groupingBy(FacRescuePersonnel::getSquadron));
            equipmentMap = equipmentMapper.selectList(new LambdaQueryWrapper<FacRescueEquipment>()
                            .in(FacRescueEquipment::getSquadron, names)
                            .orderByAsc(FacRescueEquipment::getId))
                    .stream().collect(Collectors.groupingBy(FacRescueEquipment::getSquadron));
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
            String key = row.getTeamName();
            team.setVehicles(vehicleMap.getOrDefault(key, Collections.emptyList()).stream()
                    .map(this::toBrigadeVehicle).collect(Collectors.toList()));
            team.setPersonnel(personMap.getOrDefault(key, Collections.emptyList()).stream()
                    .map(this::toBrigadePerson).collect(Collectors.toList()));
            team.setEquipment(equipmentMap.getOrDefault(key, Collections.emptyList()).stream()
                    .map(this::toBrigadeEquipment).collect(Collectors.toList()));
            teams.add(team);
        }
        return teams;
    }

    private FireBrigadeVehicle toBrigadeVehicle(FacRescueVehicle row) {
        FireBrigadeVehicle vehicle = new FireBrigadeVehicle();
        vehicle.setId(row.getId());
        vehicle.setPlate(row.getPlate());
        vehicle.setType(row.getVehicleType());
        vehicle.setStatus(row.getVehicleStatus());
        vehicle.setParkingLocation(row.getParkingLocation());
        return vehicle;
    }

    private FireBrigadePerson toBrigadePerson(FacRescuePersonnel row) {
        FireBrigadePerson person = new FireBrigadePerson();
        person.setId(row.getId());
        person.setName(row.getPersonName());
        person.setRole(row.getPersonRole());
        person.setGroup(row.getPersonGroup());
        person.setPhone(row.getPhone());
        person.setDutyStatus(row.getDutyStatus());
        return person;
    }

    private FireBrigadeEquipment toBrigadeEquipment(FacRescueEquipment row) {
        FireBrigadeEquipment equipment = new FireBrigadeEquipment();
        equipment.setId(row.getId());
        equipment.setName(row.getEquipName());
        equipment.setCategory(row.getCategory());
        equipment.setCount(row.getQuantity());
        equipment.setUnit(row.getUnit());
        equipment.setStatus(row.getEquipmentStatus());
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
