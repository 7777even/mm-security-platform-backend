package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.AlarmTrendPoint;
import com.sinopec.mmsecurity.dto.DashboardOverview;
import com.sinopec.mmsecurity.dto.RiskHeatItem;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
        return Result.ok(dashboardService.trend24h(LocalDateTime.now()));
    }

    @GetMapping("/workstations")
    public Result<List<Workstation>> workstations() {
        return Result.ok(dashboardService.workstations());
    }

    @GetMapping("/risk-heatmap")
    public Result<List<RiskHeatItem>> riskHeatmap() {
        return Result.ok(dashboardService.riskHeatmap());
    }
}
