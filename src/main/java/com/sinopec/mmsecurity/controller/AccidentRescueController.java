package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.AccidentRescueIncident;
import com.sinopec.mmsecurity.service.AccidentRescueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 事故救援应急大屏接口（fm-rescue）。
 * 单一聚合端点返回事件完整数据，避免大屏多端点拼接。
 */
@RestController
@RequestMapping("/api/v1/accident")
@RequiredArgsConstructor
public class AccidentRescueController {

    private final AccidentRescueService service;

    @GetMapping("/rescue-incident")
    public Result<AccidentRescueIncident> rescueIncident(
            @RequestParam(value = "eventId", required = false) Long eventId) {
        AccidentRescueIncident incident = service.incident(eventId);
        if (incident == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "未配置事故救援事件数据");
        }
        return Result.ok(incident);
    }
}
