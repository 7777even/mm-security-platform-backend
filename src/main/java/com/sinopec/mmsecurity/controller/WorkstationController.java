package com.sinopec.mmsecurity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.Workstation;
import com.sinopec.mmsecurity.dto.WorkstationPageResult;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.WorkstationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作站/工位防区过滤分页列表端点（数据权限 A1-①）。
 *
 * <p>登录可读（@RequireAuth，同 devices/dashboard 口径），零下行控制；复用 data_scope 行级 ABAC。
 * 与 DeviceController 同形态：分页 + zone/online 筛选。</p>
 */
@RestController
@RequestMapping("/api/v1/workstations")
@RequireAuth
@RequiredArgsConstructor
public class WorkstationController {

    private final WorkstationService workstationService;

    @GetMapping
    public Result<WorkstationPageResult> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String online) {
        Page<Workstation> p = workstationService.page(page, size, zone, online);
        WorkstationPageResult result = new WorkstationPageResult();
        result.setList(p.getRecords());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return Result.ok(result);
    }
}
