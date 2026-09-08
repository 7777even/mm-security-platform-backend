package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.FacilityDetailInfo;
import com.sinopec.mmsecurity.dto.MajorHazardDetail;
import com.sinopec.mmsecurity.dto.MajorHazardItem;
import com.sinopec.mmsecurity.dto.MonitoringAlarm;
import com.sinopec.mmsecurity.dto.MonitoringPoint;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.HazardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequireAuth
@RequiredArgsConstructor
public class HazardController {

    private final HazardService hazardService;

    @GetMapping("/hazards")
    public Result<List<MajorHazardItem>> listMajorHazards() {
        return Result.ok(hazardService.listMajorHazards());
    }

    @GetMapping("/hazards/{id}")
    public Result<MajorHazardDetail> getMajorHazardDetail(@PathVariable Long id) {
        return Result.ok(hazardService.getMajorHazardDetail(id));
    }

    @GetMapping("/monitoring/points")
    public Result<List<MonitoringPoint>> listMonitoringPoints() {
        return Result.ok(hazardService.listMonitoringPoints());
    }

    @GetMapping("/monitoring/alarms")
    public Result<List<MonitoringAlarm>> listMonitoringAlarms() {
        return Result.ok(hazardService.listMonitoringAlarms());
    }

    @GetMapping("/facilities/detail")
    public Result<FacilityDetailInfo> getFacilityDetail(@RequestParam(required = false) String name) {
        return Result.ok(hazardService.getFacilityDetail(name));
    }
}
