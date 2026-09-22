package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.FireAlarmItem;
import com.sinopec.mmsecurity.dto.FireAlarmPageResult;
import com.sinopec.mmsecurity.dto.FireAlarmUpdateRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.FireAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequireAuth
@RequiredArgsConstructor
public class FireAlarmController {

    private final FireAlarmService fireAlarmService;

    @GetMapping("/fire-alarms")
    public Result<FireAlarmPageResult> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(fireAlarmService.page(page, size));
    }

    /**
     * 消防报警写回：确认/派单/闭环状态流转 + 误报标记。需权限码 {@code fire-alarm:ack}。
     * 成功返回更新后的 FireAlarmItem（B3 包络），供前端即时回填并触发 fire-alarm.alarm 实时广播。
     */
    @PutMapping("/fire-alarms/{alarmId}")
    @RequireAuth(perm = "fire-alarm:ack")
    public Result<FireAlarmItem> update(
            @PathVariable String alarmId,
            @RequestBody FireAlarmUpdateRequest req) {
        return Result.ok(fireAlarmService.update(alarmId, req));
    }
}
