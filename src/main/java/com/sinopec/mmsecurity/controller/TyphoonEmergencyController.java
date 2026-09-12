package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.TyphoonDispatchOrderView;
import com.sinopec.mmsecurity.dto.TyphoonDispatchOrderWriteRequest;
import com.sinopec.mmsecurity.dto.TyphoonDispatchResource;
import com.sinopec.mmsecurity.dto.TyphoonEmergencyIncident;
import com.sinopec.mmsecurity.dto.TyphoonResponseBoard;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.BusinessWriteService;
import com.sinopec.mmsecurity.service.TyphoonEmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final BusinessWriteService businessWriteService;

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

    /* ==================== A2 业务写侧：台风资源调度 ==================== */

    /** 资源调度单列表（指派 / 确认 / 释放）。 */
    @GetMapping("/dispatch-orders")
    public Result<List<TyphoonDispatchOrderView>> dispatchOrders() {
        return Result.ok(businessWriteService.listDispatchOrders());
    }

    /**
     * 资源调度（指派 / 确认 / 释放）：落独立的 fac_typhoon_dispatch_order 单据表，
     * 资源清单本体（/dispatch-resources）保持只读。需权限码 {@code typhoon:dispatch:write}。
     */
    @PostMapping("/dispatch-orders")
    @RequireAuth(perm = "typhoon:dispatch:write")
    public Result<TyphoonDispatchOrderView> createDispatchOrder(
            @RequestBody TyphoonDispatchOrderWriteRequest payload) {
        return Result.ok(businessWriteService.createDispatchOrder(payload));
    }
}
