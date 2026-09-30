package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.AlarmTrendPoint;
import com.sinopec.mmsecurity.dto.DashboardOverview;
import com.sinopec.mmsecurity.dto.RiskHeatItem;
import com.sinopec.mmsecurity.dto.SystemMessageItem;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequireAuth
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardOverview> overview() {
        return Result.ok(dashboardService.overview());
    }

    @GetMapping("/alarm-trend")
    public Result<List<AlarmTrendPoint>> alarmTrend() {
        return Result.ok(dashboardService.trendDaily(LocalDateTime.now()));
    }

    @GetMapping("/workstations")
    public Result<List<Workstation>> workstations() {
        return Result.ok(dashboardService.workstations());
    }

    /** 值守工位单条明细；未命中返回 NOT_FOUND 业务码。工位主键为业务字符串（如 WS-01）。 */
    @GetMapping("/workstations/{id}")
    public Result<Workstation> workstation(@PathVariable("id") String id) {
        Workstation ws = dashboardService.workstationById(id);
        if (ws == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "值守工位不存在：" + id);
        }
        return Result.ok(ws);
    }

    @GetMapping("/risk-heatmap")
    public Result<List<RiskHeatItem>> riskHeatmap() {
        return Result.ok(dashboardService.riskHeatmap());
    }

    @GetMapping("/messages")
    public Result<List<SystemMessageItem>> messages() {
        return Result.ok(dashboardService.systemMessages());
    }
}
