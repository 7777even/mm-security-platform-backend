package com.sinopec.mmsecurity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.DeviceCode;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DevicePageResult;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@RequireAuth
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public Result<DevicePageResult> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String deviceCode) {
        Page<FacDevice> p = deviceService.page(page, size, status, zone, deviceCode);
        DevicePageResult result = new DevicePageResult();
        result.setList(p.getRecords());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return Result.ok(result);
    }

    @GetMapping("/{code}")
    public Result<FacDevice> detail(@PathVariable @DeviceCode String code) {
        return Result.ok(deviceService.byCode(code));
    }
}
