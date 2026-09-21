package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.EmergencyEventCreateRequest;
import com.sinopec.mmsecurity.dto.EmergencyEventGroup;
import com.sinopec.mmsecurity.dto.EmergencyEventItem;
import com.sinopec.mmsecurity.dto.EvacuationPerson;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.EmergencyEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 应急事件大屏只读 + 创建接口，数据源为 V17 fac_emergency_event / fac_evacuation_person 真实表。 */
@RestController
@RequestMapping("/api/v1/emergency-events")
@RequiredArgsConstructor
public class EmergencyEventController {

    private final EmergencyEventService emergencyEventService;

    /**
     * 应急事件分组列表。
     *
     * <p>scene 可选：FIRE=消防应急、PRELIMINARY=先期处置；不传时返回全部场景的分组
     * （FIRE 在前、PRELIMINARY 在后），由前端自行按分组 id 筛选。
     */
    @GetMapping
    public Result<List<EmergencyEventGroup>> eventGroups(
            @RequestParam(required = false) String scene) {
        return Result.ok(emergencyEventService.eventGroups(scene));
    }

    /** 疏散人员名册，count 为返回条数上限（默认 20），按库内 seed 顺序取前 N 条。 */
    @GetMapping("/evacuation-people")
    public Result<List<EvacuationPerson>> evacuationPeople(
            @RequestParam(defaultValue = "20") Integer count) {
        return Result.ok(emergencyEventService.evacuationPeople(count));
    }

    /**
     * 新增应急事件（仅登录态）。后端同事务写入 fac_emergency_event 与 fac_accident_incident，
     * 返回后端生成的真实事件 id，供「去处置」直接按 event_id 定位（不再回退默认事件）。
     */
    @PostMapping
    @RequireAuth
    public Result<EmergencyEventItem> create(@Valid @RequestBody EmergencyEventCreateRequest payload) {
        return Result.ok(emergencyEventService.create(payload));
    }

    /**
     * 事件预警（报送）。标记应急事件已预警（reported=true），并同步关联事故救援事件
     * （fac_accident_incident）的 reported 标志。仅登录态（值守/指挥人员自助），按变更决策取
     * 「仅登录态」（非 ADMIN）；事件不存在返回 404。与 create 同源的自助写入场景。
     */
    @PostMapping("/{id}/report")
    @RequireAuth
    public Result<EmergencyEventItem> report(@PathVariable("id") Long id) {
        return Result.ok(emergencyEventService.report(id));
    }

    /**
     * 启动应急响应。将事件状态推进为「处置中」（processing），并同步关联事故救援事件。
     * 仅登录态（值守/指挥人员自助）；事件不存在返回 404。与 report/create 同源的自助写入场景。
     */
    @PostMapping("/{id}/start-response")
    @RequireAuth
    public Result<EmergencyEventItem> startResponse(@PathVariable("id") Long id) {
        return Result.ok(emergencyEventService.startResponse(id));
    }
}
