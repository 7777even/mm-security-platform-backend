package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.EmergencyPlanOptions;
import com.sinopec.mmsecurity.dto.PlanInstance;
import com.sinopec.mmsecurity.service.EmergencyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 应急预案大屏只读接口，数据源为 V18 fac_emergency_plan / fac_plan_* 真实表。 */
@RestController
@RequestMapping("/api/v1/emergency-plans")
@RequiredArgsConstructor
public class EmergencyPlanController {

    private final EmergencyPlanService emergencyPlanService;

    /** 预案切换面板选项：页签 + 事故类型/装置筛选字典 + 预案目录。 */
    @GetMapping("/options")
    public Result<EmergencyPlanOptions> options() {
        return Result.ok(emergencyPlanService.options());
    }

    /** 预案矩阵：按 planId 返回预案实例，缺省或未命中时返回默认预案。 */
    @GetMapping("/matrix")
    public Result<PlanInstance> matrix(@RequestParam(required = false) String planId) {
        return Result.ok(emergencyPlanService.matrix(planId));
    }
}
