package com.sinopec.mmsecurity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.DeviceCode;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.DevicePageResult;
import com.sinopec.mmsecurity.dto.DeviceWriteRequest;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /** 新建设备台账（deviceCode 为 20 位 MDM 编码，由请求体给定）。 */
    @PostMapping
    @RequireAuth(role = "ADMIN")
    public Result<FacDevice> create(@Valid @RequestBody DeviceWriteRequest payload) {
        return Result.ok(deviceService.createDevice(payload));
    }

    /** 更新设备台账（按 deviceCode）；未命中或已软删时 data 为 null。 */
    @PutMapping("/{code}")
    @RequireAuth(role = "ADMIN")
    public Result<FacDevice> update(@PathVariable @DeviceCode String code,
                                    @Valid @RequestBody DeviceWriteRequest payload) {
        return Result.ok(deviceService.updateDevice(code, payload));
    }

    /** 删除设备台账（软删除，置 deleted=1）；未命中 ok=false。 */
    @DeleteMapping("/{code}")
    @RequireAuth(role = "ADMIN")
    public Result<DeleteResult> delete(@PathVariable @DeviceCode String code) {
        return Result.ok(deviceService.deleteDevice(code));
    }
}
