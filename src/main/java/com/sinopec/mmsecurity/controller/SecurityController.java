package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.BollardItem;
import com.sinopec.mmsecurity.dto.BollardWriteRequest;
import com.sinopec.mmsecurity.dto.GateControlItem;
import com.sinopec.mmsecurity.dto.GateControlWriteRequest;
import com.sinopec.mmsecurity.dto.PatrolCameraItem;
import com.sinopec.mmsecurity.dto.PerimeterAlarmDetail;
import com.sinopec.mmsecurity.dto.PersonSearchDetail;
import com.sinopec.mmsecurity.dto.PersonSearchResult;
import com.sinopec.mmsecurity.dto.PersonSearchWriteRequest;
import com.sinopec.mmsecurity.dto.PerimeterAlarmCreateRequest;
import com.sinopec.mmsecurity.dto.PerimeterAlarmUpdateRequest;
import com.sinopec.mmsecurity.dto.SecurityEvent;
import com.sinopec.mmsecurity.dto.SecurityTrackSummary;
import com.sinopec.mmsecurity.dto.SecurityTrackTimelineItem;
import com.sinopec.mmsecurity.dto.VehicleSearchDetail;
import com.sinopec.mmsecurity.dto.VehicleSearchResult;
import com.sinopec.mmsecurity.dto.VehicleSearchWriteRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    /**
     * 道闸台账新增。status 为设备实时状态，仅读不写（零下行控制红线），请求体不含该字段。
     * 需权限码 {@code security:gate-write}（V101 登记并授权岗位角色），成功后广播 security.gate-control。
     */
    @PostMapping("/security/gate-controls")
    @RequireAuth(perm = "security:gate-write")
    public Result<GateControlItem> createGate(
            @Valid @RequestBody GateControlWriteRequest req) {
        return Result.ok(securityService.createGate(req));
    }

    @PutMapping("/security/gate-controls/{id}")
    @RequireAuth(perm = "security:gate-write")
    public Result<GateControlItem> updateGate(
            @PathVariable Long id,
            @Valid @RequestBody GateControlWriteRequest req) {
        return Result.ok(securityService.updateGate(id, req));
    }

    @DeleteMapping("/security/gate-controls/{id}")
    @RequireAuth(perm = "security:gate-write")
    public Result<Void> deleteGate(@PathVariable Long id) {
        securityService.deleteGate(id);
        return Result.ok(null);
    }

    /**
     * 防恐柱台账新增。status 仅读不写（零下行控制红线）。
     * 需权限码 {@code security:bollard-write}（V101），成功后广播 security.bollard。
     */
    @PostMapping("/security/bollards")
    @RequireAuth(perm = "security:bollard-write")
    public Result<BollardItem> createBollard(
            @Valid @RequestBody BollardWriteRequest req) {
        return Result.ok(securityService.createBollard(req));
    }

    @PutMapping("/security/bollards/{id}")
    @RequireAuth(perm = "security:bollard-write")
    public Result<BollardItem> updateBollard(
            @PathVariable Long id,
            @Valid @RequestBody BollardWriteRequest req) {
        return Result.ok(securityService.updateBollard(id, req));
    }

    @DeleteMapping("/security/bollards/{id}")
    @RequireAuth(perm = "security:bollard-write")
    public Result<Void> deleteBollard(@PathVariable Long id) {
        securityService.deleteBollard(id);
        return Result.ok(null);
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

    @PostMapping("/security/search/person")
    @RequireAuth(perm = "security:person-write")
    public Result<PersonSearchDetail> createPerson(
            @Valid @RequestBody PersonSearchWriteRequest req) {
        return Result.ok(securityService.createPerson(req));
    }

    @PutMapping("/security/search/person/{id}")
    @RequireAuth(perm = "security:person-write")
    public Result<PersonSearchDetail> updatePerson(
            @PathVariable Long id,
            @Valid @RequestBody PersonSearchWriteRequest req) {
        return Result.ok(securityService.updatePerson(id, req));
    }

    @DeleteMapping("/security/search/person/{id}")
    @RequireAuth(perm = "security:person-write")
    public Result<Void> deletePerson(@PathVariable Long id) {
        securityService.deletePerson(id);
        return Result.ok(null);
    }

    @PostMapping("/security/search/vehicle")
    @RequireAuth(perm = "security:vehicle-write")
    public Result<VehicleSearchDetail> createVehicle(
            @Valid @RequestBody VehicleSearchWriteRequest req) {
        return Result.ok(securityService.createVehicle(req));
    }

    @PutMapping("/security/search/vehicle/{id}")
    @RequireAuth(perm = "security:vehicle-write")
    public Result<VehicleSearchDetail> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleSearchWriteRequest req) {
        return Result.ok(securityService.updateVehicle(id, req));
    }

    @DeleteMapping("/security/search/vehicle/{id}")
    @RequireAuth(perm = "security:vehicle-write")
    public Result<Void> deleteVehicle(@PathVariable Long id) {
        securityService.deleteVehicle(id);
        return Result.ok(null);
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

    /**
     * 周界入侵告警写回：确认/派单/处置状态流转 + 误报标记 + 处置情况/时间/派单人员/通知方式局部更新。
     * 需权限码 {@code security:perimeter-ack}（V70 已登记并授权 ADMIN 及岗位角色）。
     * 成功返回更新后的 PerimeterAlarmDetail（B3 包络），供前端即时回填并触发 security.perimeter-alarm 实时广播。
     * 与消防报警写回（FireAlarmController#update）同源范式，状态使用中文枚举（未确认/已确认/已派单/已处理）。
     */
    @PutMapping("/security/perimeter-alarms/{id}")
    @RequireAuth(perm = "security:perimeter-ack")
    public Result<PerimeterAlarmDetail> updatePerimeterAlarm(
            @PathVariable Long id,
            @RequestBody PerimeterAlarmUpdateRequest req) {
        return Result.ok(securityService.updatePerimeterAlarm(id, req));
    }

    @PostMapping("/security/perimeter-alarms")
    @RequireAuth(perm = "security:perimeter-create")
    public Result<PerimeterAlarmDetail> createPerimeterAlarm(
            @RequestBody PerimeterAlarmCreateRequest req) {
        return Result.ok(securityService.createPerimeterAlarm(req));
    }

    @DeleteMapping("/security/perimeter-alarms/{id}")
    @RequireAuth(perm = "security:perimeter-delete")
    public Result<Void> deletePerimeterAlarm(@PathVariable Long id) {
        securityService.deletePerimeterAlarm(id);
        return Result.ok(null);
    }
}
