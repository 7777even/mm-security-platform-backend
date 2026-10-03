package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.CommDeviceWriteRequest;
import com.sinopec.mmsecurity.dto.CommunicationDevice;
import com.sinopec.mmsecurity.dto.CommunicationDeviceGroups;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.CommDeviceService;
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

/**
 * 通讯设备（广播 / 电话 / 对讲）只读接口，数据源为 V22 fac_comm_device 真实表。
 *
 * <p>本组仅提供 GET：列表按页签分组返回、详情按设备编码返回。
 */
@RestController
@RequestMapping("/api/v1/communication")
@RequiredArgsConstructor
public class CommDeviceController {

    private final CommDeviceService commDeviceService;

    /**
     * 通讯设备分组列表（一次返回广播/电话/对讲三个页签的分组）。
     *
     * <p>tab 参数仅为兼容前端面板的页签语义而保留：<b>服务端不做过滤，始终返回完整的三组数据</b>，
     * 由前端按当前页签自行选择要渲染的分组。这样可避免切页签时重复请求，也省去无效 tab 值的校验分支。
     */
    @GetMapping("/devices")
    public Result<CommunicationDeviceGroups> devices(
            @RequestParam(value = "tab", required = false) String tab) {
        return Result.ok(commDeviceService.groups());
    }

    /** 新建通讯设备台账（id 与 sort_no 由服务端分配，deviceCode 为业务自然键）。 */
    @PostMapping("/devices")
    @RequireAuth(role = "ADMIN")
    public Result<CommunicationDevice> createDevice(@Valid @RequestBody CommDeviceWriteRequest payload) {
        return Result.ok(commDeviceService.createDevice(payload));
    }

    /** 更新通讯设备台账（按 deviceCode，与 GET 详情一致）。 */
    @PutMapping("/devices/{id}")
    @RequireAuth(role = "ADMIN")
    public Result<CommunicationDevice> updateDevice(@PathVariable("id") String id, @Valid @RequestBody CommDeviceWriteRequest payload) {
        return Result.ok(commDeviceService.updateDevice(id, payload));
    }

    /** 删除通讯设备台账（按 deviceCode）。 */
    @DeleteMapping("/devices/{id}")
    @RequireAuth(role = "ADMIN")
    public Result<DeleteResult> deleteDevice(@PathVariable("id") String id) {
        return Result.ok(commDeviceService.deleteDevice(id));
    }

    /** 按设备编码查单台设备（如 bc-a1），用于地图点击后的详情面板；未命中时 data 返回 null。 */
    @GetMapping("/devices/{id}")
    public Result<CommunicationDevice> device(@PathVariable("id") String id) {
        return Result.ok(commDeviceService.byCode(id));
    }
}
