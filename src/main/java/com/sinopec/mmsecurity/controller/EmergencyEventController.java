package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.EmergencyEventGroup;
import com.sinopec.mmsecurity.dto.EvacuationPerson;
import com.sinopec.mmsecurity.service.EmergencyEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 应急事件大屏只读接口，数据源为 V17 fac_emergency_event / fac_evacuation_person 真实表。 */
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
}
