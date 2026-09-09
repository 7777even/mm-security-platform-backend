package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.FireBrigadeList;
import com.sinopec.mmsecurity.dto.FireBrigadeTeam;
import com.sinopec.mmsecurity.dto.RescueEquipmentItem;
import com.sinopec.mmsecurity.dto.RescueEquipmentList;
import com.sinopec.mmsecurity.dto.RescuePersonnelItem;
import com.sinopec.mmsecurity.dto.RescuePersonnelList;
import com.sinopec.mmsecurity.dto.RescueVehicleItem;
import com.sinopec.mmsecurity.dto.RescueVehicleList;
import com.sinopec.mmsecurity.service.RescueResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应急救援资源只读接口（救援装备 / 救援人员 / 救援车辆 / 消防队伍）。
 * 数据源为 V19 fac_rescue_* 与 fac_brigade_* 真实表，全部为 GET 查询。
 */
@RestController
@RequestMapping("/api/v1/rescue-resources")
@RequiredArgsConstructor
public class RescueResourceController {

    private final RescueResourceService rescueResourceService;

    /** 救援装备列表：可按中队过滤（不传或传“全部中队”表示全部）。 */
    @GetMapping("/equipment")
    public Result<RescueEquipmentList> equipment(
            @RequestParam(required = false) String squadron) {
        return Result.ok(rescueResourceService.equipment(squadron));
    }

    /** 救援装备详情：按 id 查询，不存在时返回业务错误码 404。 */
    @GetMapping("/equipment/{id}")
    public Result<RescueEquipmentItem> equipmentDetail(@PathVariable Long id) {
        return Result.ok(rescueResourceService.equipmentDetail(id));
    }

    /** 救援人员列表：可按中队 / 岗位过滤。 */
    @GetMapping("/personnel")
    public Result<RescuePersonnelList> personnel(
            @RequestParam(required = false) String squadron,
            @RequestParam(required = false) String role) {
        return Result.ok(rescueResourceService.personnel(squadron, role));
    }

    /** 救援人员详情：按 id 查询，不存在时返回业务错误码 404。 */
    @GetMapping("/personnel/{id}")
    public Result<RescuePersonnelItem> personnelDetail(@PathVariable Long id) {
        return Result.ok(rescueResourceService.personnelDetail(id));
    }

    /** 救援车辆列表：可按中队 / 类型过滤，条目含乘员、随车装备、耗材与出动汇总。 */
    @GetMapping("/vehicles")
    public Result<RescueVehicleList> vehicles(
            @RequestParam(required = false) String squadron,
            @RequestParam(required = false) String type) {
        return Result.ok(rescueResourceService.vehicles(squadron, type));
    }

    /** 救援车辆详情：按 id 查询，含子集合，不存在时返回业务错误码 404。 */
    @GetMapping("/vehicles/{id}")
    public Result<RescueVehicleItem> vehicleDetail(@PathVariable Long id) {
        return Result.ok(rescueResourceService.vehicleDetail(id));
    }

    /** 消防队伍列表：可按区域过滤，条目含队伍车辆 / 人员 / 装备。 */
    @GetMapping("/brigades")
    public Result<FireBrigadeList> brigades(
            @RequestParam(required = false) String area) {
        return Result.ok(rescueResourceService.brigades(area));
    }

    /** 消防队伍详情：按 id 查询，含子集合，不存在时返回业务错误码 404。 */
    @GetMapping("/brigades/{id}")
    public Result<FireBrigadeTeam> brigadeDetail(@PathVariable Long id) {
        return Result.ok(rescueResourceService.brigadeDetail(id));
    }
}
