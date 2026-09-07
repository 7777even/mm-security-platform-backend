package com.sinopec.mmsecurity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.DeviceCode;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/alarms")
@RequireAuth
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deviceCode) {
        Page<FacAlarm> p = alarmService.page(page, size, level, status, deviceCode);
        if (p.getTotal() == 0) {
            var fallback = alarmService.devFallbackList(8);
            return Result.ok(Map.of("list", fallback, "total", fallback.size(), "page", 1, "size", size, "mock", true));
        }
        return Result.ok(Map.of("list", p.getRecords(), "total", p.getTotal(), "page", p.getCurrent(), "size", p.getSize()));
    }
}
