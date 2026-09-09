package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.FireSituationMarkerSummary;
import com.sinopec.mmsecurity.service.FireSituationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 火情态势大屏地图点位只读接口，数据源为 V21 fac_fire_situation_marker 真实表。 */
@RestController
@RequestMapping("/api/v1/fire-situation")
@RequiredArgsConstructor
public class FireSituationController {

    private final FireSituationService fireSituationService;

    /** 地图聚合点位列表（应急事件 / 作业票 / 报警）。 */
    @GetMapping("/markers")
    public Result<FireSituationMarkerSummary> markers() {
        return Result.ok(fireSituationService.markers());
    }
}
