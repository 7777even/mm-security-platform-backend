package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.BollardItem;
import com.sinopec.mmsecurity.dto.GateControlItem;
import com.sinopec.mmsecurity.dto.PatrolCameraItem;
import com.sinopec.mmsecurity.dto.PerimeterAlarmDetail;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    /**
     * 最新一条周界入侵告警（按告警时间倒序）。表为空时 data=null，前端按「无告警」渲染。
     * 替代前端 SecurityStatusPanel 的 demo 常量 resolveDemoAlarmDetailById('demo-intrusion-1')。
     */
    @GetMapping("/security/perimeter-alarms/latest")
    public Result<PerimeterAlarmDetail> latestPerimeterAlarm() {
        return Result.ok(securityService.latestPerimeterAlarm());
    }

    @GetMapping("/security/perimeter-alarms/{id}")
    public Result<PerimeterAlarmDetail> perimeterAlarm(@PathVariable Long id) {
        return Result.ok(securityService.perimeterAlarmDetail(id));
    }

    /**
     * 周界入侵告警现场抓拍：返回 snapshot_bytes 列中的 JPEG 字节。
     * 鉴权同 /security/*（需 JWT），前端用带 token 的 http 客户端取 blob 后转 objectURL 渲染。
     */
    @GetMapping("/security/perimeter-alarms/{id}/snapshot")
    public ResponseEntity<Resource> perimeterAlarmSnapshot(@PathVariable Long id) {
        byte[] bytes = securityService.perimeterAlarmSnapshot(id);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(new ByteArrayResource(bytes));
    }
}
