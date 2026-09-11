package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.FireMonitorAreaSummary;
import com.sinopec.mmsecurity.dto.FireMonitoredObjectSummary;
import com.sinopec.mmsecurity.dto.FireSituationMarkerSummary;
import com.sinopec.mmsecurity.service.FireSituationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 火情态势大屏只读接口，数据源为 V21 fac_fire_situation_marker / V38 fac_fire_monitor_area 等真实表。 */
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

    /** 各装置区消防保障汇总（设备 / 视频 / 人员 / 状态）。 */
    @GetMapping("/areas")
    public Result<FireMonitorAreaSummary> areas() {
        return Result.ok(fireSituationService.areaSummary());
    }

    /** 重点监控对象列表（状态 / 详情 / 配色）。 */
    @GetMapping("/monitored-objects")
    public Result<FireMonitoredObjectSummary> monitoredObjects() {
        return Result.ok(fireSituationService.monitoredObjects());
    }
}
