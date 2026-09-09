package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.FireEquipmentStatus;
import com.sinopec.mmsecurity.dto.FirePatrolRecord;
import com.sinopec.mmsecurity.dto.RescueForceStat;
import com.sinopec.mmsecurity.dto.SpecialOperationStat;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.FireMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
