package com.sinopec.mmsecurity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.AlarmPageResult;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alarms")
@RequireAuth
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public Result<AlarmPageResult> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deviceCode) {
        Page<FacAlarm> p = alarmService.page(page, size, level, status, deviceCode);
        AlarmPageResult result = new AlarmPageResult();
        result.setList(p.getRecords());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return Result.ok(result);
    }
}
