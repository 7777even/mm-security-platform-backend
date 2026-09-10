package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.BollardItem;
import com.sinopec.mmsecurity.dto.GateControlItem;
import com.sinopec.mmsecurity.dto.PatrolCameraItem;
import com.sinopec.mmsecurity.dto.PersonSearchDetail;
import com.sinopec.mmsecurity.dto.PersonSearchResult;
import com.sinopec.mmsecurity.dto.SecurityEvent;
import com.sinopec.mmsecurity.dto.SecurityTrackSummary;
import com.sinopec.mmsecurity.dto.SecurityTrackTimelineItem;
import com.sinopec.mmsecurity.dto.VehicleSearchDetail;
import com.sinopec.mmsecurity.dto.VehicleSearchResult;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.SecurityService;
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
public class SecurityController {

    private final SecurityService securityService;

    @GetMapping("/security/patrol-cameras")
    public Result<List<PatrolCameraItem>> listPatrolCameras() {
        return Result.ok(securityService.listPatrolCameras());
    }

    @GetMapping("/security/gate-controls")
    public Result<List<GateControlItem>> listGateControls() {
        return Result.ok(securityService.listGateControls());
    }

    @GetMapping("/security/bollards")
    public Result<List<BollardItem>> listBollards() {
        return Result.ok(securityService.listBollards());
    }

    @GetMapping("/security/search/vehicle")
    public Result<List<VehicleSearchResult>> searchVehicles(
            @RequestParam(required = false) String keyword) {
        return Result.ok(securityService.searchVehicles(keyword));
    }

    @GetMapping("/security/search/person")
    public Result<List<PersonSearchResult>> searchPersons(
            @RequestParam(required = false) String keyword) {
        return Result.ok(securityService.searchPersons(keyword));
    }

    @GetMapping("/security/events")
    public Result<List<SecurityEvent>> listSecurityEvents() {
        return Result.ok(securityService.listSecurityEvents());
    }

    @GetMapping("/security/track/timeline")
    public Result<List<SecurityTrackTimelineItem>> trackTimeline(
            @RequestParam String mode,
            @RequestParam(required = false) Long entityId) {
        return Result.ok(securityService.trackTimeline(mode, entityId));
    }

    @GetMapping("/security/track/summary")
    public Result<SecurityTrackSummary> trackSummary(
            @RequestParam String mode,
            @RequestParam(required = false) Long entityId) {
        return Result.ok(securityService.trackSummary(mode, entityId));
    }

    @GetMapping("/security/search/vehicle/{id}")
    public Result<VehicleSearchDetail> vehicleDetail(@PathVariable Long id) {
        return Result.ok(securityService.vehicleDetail(id));
    }

    @GetMapping("/security/search/person/{id}")
    public Result<PersonSearchDetail> personDetail(@PathVariable Long id) {
        return Result.ok(securityService.personDetail(id));
    }
}
