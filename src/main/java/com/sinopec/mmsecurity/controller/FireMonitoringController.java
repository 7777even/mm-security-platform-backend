package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.FireEquipmentStatus;
import com.sinopec.mmsecurity.dto.FireEquipmentItem;
import com.sinopec.mmsecurity.dto.FirePatrolRecord;
import com.sinopec.mmsecurity.dto.PatrolExecutionView;
import com.sinopec.mmsecurity.dto.PatrolExecutionWriteRequest;
import com.sinopec.mmsecurity.dto.RescueForceStat;
import com.sinopec.mmsecurity.dto.SpecialOperationStat;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.BusinessWriteService;
import com.sinopec.mmsecurity.service.FireMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 消防监控大屏（fm-fire）统计与巡查接口，全部读取 V10 落地的真实表。 */
@RestController
@RequestMapping("/api/v1/fire")
@RequireAuth
@RequiredArgsConstructor
public class FireMonitoringController {

    private final FireMonitoringService fireMonitoringService;
    private final BusinessWriteService businessWriteService;

    @GetMapping("/rescue-forces")
    public Result<List<RescueForceStat>> rescueForces() {
        return Result.ok(fireMonitoringService.rescueForces());
    }

    @GetMapping("/special-operations")
    public Result<List<SpecialOperationStat>> specialOperations() {
        return Result.ok(fireMonitoringService.specialOperations());
    }

    @GetMapping("/equipment-status")
    public Result<FireEquipmentStatus> equipmentStatus() {
        return Result.ok(fireMonitoringService.equipmentStatus());
    }

    @GetMapping("/patrols")
    public Result<List<FirePatrolRecord>> patrols() {
        return Result.ok(fireMonitoringService.patrols());
    }

    @GetMapping("/equipment")
    public Result<List<FireEquipmentItem>> equipment() {
        return Result.ok(fireMonitoringService.equipment());
    }

    /* ==================== A2 业务写侧：巡更执行上报 ==================== */

    /** 巡更执行记录列表（打卡与结果）。 */
    @GetMapping("/patrol-executions")
    public Result<List<PatrolExecutionView>> patrolExecutions() {
        return Result.ok(businessWriteService.listPatrolExecutions());
    }

    /**
     * 巡更执行上报（打卡与结果）：落独立的 fac_patrol_execution 表，
     * 巡查计划本体（/patrols）保持只读。需权限码 {@code fire-alarm:patrol:write}。
     */
    @PostMapping("/patrol-executions")
    @RequireAuth(perm = "fire-alarm:patrol:write")
    public Result<PatrolExecutionView> createPatrolExecution(
            @RequestBody PatrolExecutionWriteRequest payload) {
        return Result.ok(businessWriteService.createPatrolExecution(payload));
    }
}
