package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.TyphoonDispatchResource;
import com.sinopec.mmsecurity.dto.TyphoonEmergencyIncident;
import com.sinopec.mmsecurity.dto.TyphoonResponseBoard;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.TyphoonEmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 台风应急大屏（fm-typhoon）接口，读取 V11 落地的 fac_typhoon_* 真实表。 */
@RestController
@RequestMapping("/api/v1/typhoon")
@RequireAuth
@RequiredArgsConstructor
public class TyphoonEmergencyController {

    private final TyphoonEmergencyService typhoonEmergencyService;

    @GetMapping("/incident")
    public Result<TyphoonEmergencyIncident> incident(
            @RequestParam(required = false) Long eventId) {
        TyphoonEmergencyIncident data = typhoonEmergencyService.incident(eventId);
        if (data == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "未找到台风应急事件");
        }
        return Result.ok(data);
    }

    @GetMapping("/dispatch-resources")
    public Result<List<TyphoonDispatchResource>> dispatchResources() {
        return Result.ok(typhoonEmergencyService.dispatchResources());
    }

    /** 台风应急响应板聚合（V41 fac_typhoon_alert_banner / fac_typhoon_command）。 */
    @GetMapping("/response-board")
    public Result<TyphoonResponseBoard> responseBoard() {
        return Result.ok(typhoonEmergencyService.responseBoard());
    }
}
