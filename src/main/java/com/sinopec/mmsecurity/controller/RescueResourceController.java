package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
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
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.RescueResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应急救援资源接口（救援装备 / 救援人员 / 救援车辆 / 消防队伍）。
 *
 * <p>数据源为 V19 fac_rescue_* 与 fac_brigade_* 真实表。读端点仅需登录态；
 * 写端点（POST / PUT / DELETE）受各资源按钮级权限码控制（V93 登记并授权）：
 * {@code rescue:personnel:write} / {@code rescue:brigade:write} /
 * {@code rescue:vehicle:write} / {@code rescue:equipment:write}。
 * 写操作统一标记 {@code @RealtimeSync}，广播 rescue.* 域，三端实时刷新。
 *
 * <p>编辑一律是<b>局部更新</b>（字段为 null 表示不修改）；删除为物理删除（四张表均无 deleted 列）。
 * 全线属业务台账留痕，<b>不触发任何物理设备</b>，与零下行红线无关。</p>
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

    /* ==================== 写侧：救援人员（应急专家） ==================== */

    /**
     * 新增救援人员（应急专家）。name 必填；需 {@code rescue:personnel:write}（V93 登记）。
     * 成功广播 rescue.personnel 域。
     */
    @PostMapping("/personnel")
    @RequireAuth(perm = "rescue:personnel:write")
    public Result<RescuePersonnelItem> createPersonnel(
            @RequestBody RescuePersonnelWriteRequest payload) {
        return Result.ok(rescueResourceService.createPersonnel(payload));
    }

    /** 编辑救援人员（局部更新）。不存在返回 B3 NOT_FOUND。 */
    @PutMapping("/personnel/{id}")
    @RequireAuth(perm = "rescue:personnel:write")
    public Result<RescuePersonnelItem> updatePersonnel(
            @PathVariable Long id,
            @RequestBody RescuePersonnelWriteRequest payload) {
        return Result.ok(rescueResourceService.updatePersonnel(id, payload));
    }

    /** 删除救援人员（物理删除）。不存在（含重复删除）返回 B3 NOT_FOUND。 */
    @DeleteMapping("/personnel/{id}")
    @RequireAuth(perm = "rescue:personnel:write")
    public Result<Void> deletePersonnel(@PathVariable Long id) {
        rescueResourceService.deletePersonnel(id);
        return Result.ok(null);
    }

    /* ==================== 写侧：消防队伍（救援队伍） ==================== */

    /** 新增消防队伍。name 必填；需 {@code rescue:brigade:write}。 */
    @PostMapping("/brigades")
    @RequireAuth(perm = "rescue:brigade:write")
    public Result<FireBrigadeTeam> createBrigade(@RequestBody RescueBrigadeWriteRequest payload) {
        return Result.ok(rescueResourceService.createBrigade(payload));
    }

    /** 编辑消防队伍（局部更新）。不存在返回 B3 NOT_FOUND。 */
    @PutMapping("/brigades/{id}")
    @RequireAuth(perm = "rescue:brigade:write")
    public Result<FireBrigadeTeam> updateBrigade(
            @PathVariable Long id,
            @RequestBody RescueBrigadeWriteRequest payload) {
        return Result.ok(rescueResourceService.updateBrigade(id, payload));
    }

    /** 删除消防队伍（物理删除）。不存在返回 B3 NOT_FOUND。 */
    @DeleteMapping("/brigades/{id}")
    @RequireAuth(perm = "rescue:brigade:write")
    public Result<Void> deleteBrigade(@PathVariable Long id) {
        rescueResourceService.deleteBrigade(id);
        return Result.ok(null);
    }

    /* ==================== 写侧：救援车辆 ==================== */

    /** 新增救援车辆。plate 必填；需 {@code rescue:vehicle:write}。 */
    @PostMapping("/vehicles")
    @RequireAuth(perm = "rescue:vehicle:write")
    public Result<RescueVehicleItem> createVehicle(@RequestBody RescueVehicleWriteRequest payload) {
        return Result.ok(rescueResourceService.createVehicle(payload));
    }

    /** 编辑救援车辆（局部更新）。不存在返回 B3 NOT_FOUND。 */
    @PutMapping("/vehicles/{id}")
    @RequireAuth(perm = "rescue:vehicle:write")
    public Result<RescueVehicleItem> updateVehicle(
            @PathVariable Long id,
            @RequestBody RescueVehicleWriteRequest payload) {
        return Result.ok(rescueResourceService.updateVehicle(id, payload));
    }

    /** 删除救援车辆（物理删除）。不存在返回 B3 NOT_FOUND。 */
    @DeleteMapping("/vehicles/{id}")
    @RequireAuth(perm = "rescue:vehicle:write")
    public Result<Void> deleteVehicle(@PathVariable Long id) {
        rescueResourceService.deleteVehicle(id);
        return Result.ok(null);
    }

    /* ==================== 写侧：救援装备（应急物资） ==================== */

    /** 新增救援装备。name 必填；需 {@code rescue:equipment:write}。 */
    @PostMapping("/equipment")
    @RequireAuth(perm = "rescue:equipment:write")
    public Result<RescueEquipmentItem> createEquipment(
            @RequestBody RescueEquipmentWriteRequest payload) {
        return Result.ok(rescueResourceService.createEquipment(payload));
    }

    /** 编辑救援装备（局部更新）。不存在返回 B3 NOT_FOUND。 */
    @PutMapping("/equipment/{id}")
    @RequireAuth(perm = "rescue:equipment:write")
    public Result<RescueEquipmentItem> updateEquipment(
            @PathVariable Long id,
            @RequestBody RescueEquipmentWriteRequest payload) {
        return Result.ok(rescueResourceService.updateEquipment(id, payload));
    }

    /** 删除救援装备（物理删除）。不存在返回 B3 NOT_FOUND。 */
    @DeleteMapping("/equipment/{id}")
    @RequireAuth(perm = "rescue:equipment:write")
    public Result<Void> deleteEquipment(@PathVariable Long id) {
        rescueResourceService.deleteEquipment(id);
        return Result.ok(null);
    }
}
