package com.sinopec.mmsecurity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.DeviceCode;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/devices")
@RequireAuth
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String deviceCode) {
        Page<FacDevice> p = deviceService.page(page, size, status, zone, deviceCode);
        // 兼容空库：查不到数据时回落到模拟值，便于开发联调
        if (p.getTotal() == 0) {
            List<FacDevice> fallback = deviceService.devFallbackList(12);
            return Result.ok(Map.of("list", fallback, "total", fallback.size(), "page", 1, "size", size, "mock", true));
        }
        return Result.ok(Map.of("list", p.getRecords(), "total", p.getTotal(), "page", p.getCurrent(), "size", p.getSize()));
    }

    @GetMapping("/{code}")
    public Result<FacDevice> detail(@PathVariable @DeviceCode String code) {
        return Result.ok(deviceService.byCode(code));
    }
}
