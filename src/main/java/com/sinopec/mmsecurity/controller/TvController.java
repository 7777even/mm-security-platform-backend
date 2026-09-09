package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.TvInspectionSummary;
import com.sinopec.mmsecurity.dto.TvOverview;
import com.sinopec.mmsecurity.service.TvService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 工业电视大屏（fm-tv）只读接口，数据源为 V15 fac_tv_* 真实表。 */
@RestController
@RequestMapping("/api/v1/tv")
@RequiredArgsConstructor
public class TvController {

    private final TvService tvService;

    /** 首屏聚合：概览卡片 + 运行统计 + 维保工单 + 事件分析。 */
    @GetMapping("/overview")
    public Result<TvOverview> overview() {
        return Result.ok(tvService.overview());
    }

    /** 入厂巡检聚合：车辆列表 + 人员列表。 */
    @GetMapping("/inspections")
    public Result<TvInspectionSummary> inspections() {
        return Result.ok(tvService.inspections());
    }
}
