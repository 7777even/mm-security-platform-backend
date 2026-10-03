package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.FacilityDetailInfo;
import com.sinopec.mmsecurity.dto.MajorHazardDetail;
import com.sinopec.mmsecurity.dto.MajorHazardItem;
import com.sinopec.mmsecurity.dto.MajorHazardWriteRequest;
import com.sinopec.mmsecurity.dto.MonitoringAlarm;
import com.sinopec.mmsecurity.dto.MonitoringPoint;
import com.sinopec.mmsecurity.dto.MonitoringPointWriteRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.HazardService;
import jakarta.validation.Valid;
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

    /** 新建重大危险源（id 由服务端分配）。 */
    @PostMapping("/hazards")
    @RequireAuth(perm = "hazard:write")
    public Result<MajorHazardItem> createHazard(@Valid @RequestBody MajorHazardWriteRequest payload) {
        return Result.ok(hazardService.createHazard(payload));
    }

    /** 更新重大危险源（按 id）；未命中 data 为 null。 */
    @PutMapping("/hazards/{id}")
    @RequireAuth(perm = "hazard:write")
    public Result<MajorHazardItem> updateHazard(@PathVariable Long id,
                                                @Valid @RequestBody MajorHazardWriteRequest payload) {
        return Result.ok(hazardService.updateHazard(id, payload));
    }

    /** 删除重大危险源（按 id）；未命中 ok=false。 */
    @DeleteMapping("/hazards/{id}")
    @RequireAuth(perm = "hazard:write")
    public Result<DeleteResult> deleteHazard(@PathVariable Long id) {
        return Result.ok(hazardService.deleteHazard(id));
    }

    /** 新建监测点位（id 为字符串主键，由请求体给定；重复返回 409）。 */
    @PostMapping("/monitoring/points")
    @RequireAuth(perm = "hazard:point-write")
    public Result<MonitoringPoint> createPoint(@Valid @RequestBody MonitoringPointWriteRequest payload) {
        return Result.ok(hazardService.createPoint(payload));
    }

    /** 更新监测点位（按 id）；未命中 data 为 null。 */
    @PutMapping("/monitoring/points/{id}")
    @RequireAuth(perm = "hazard:point-write")
    public Result<MonitoringPoint> updatePoint(@PathVariable String id,
                                               @Valid @RequestBody MonitoringPointWriteRequest payload) {
        return Result.ok(hazardService.updatePoint(id, payload));
    }

    /** 删除监测点位（按 id）；未命中 ok=false。 */
    @DeleteMapping("/monitoring/points/{id}")
    @RequireAuth(perm = "hazard:point-write")
    public Result<DeleteResult> deletePoint(@PathVariable String id) {
        return Result.ok(hazardService.deletePoint(id));
    }
}
