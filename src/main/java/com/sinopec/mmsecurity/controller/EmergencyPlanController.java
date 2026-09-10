package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.EmergencyPlanOptions;
import com.sinopec.mmsecurity.dto.PlanActionCard;
import com.sinopec.mmsecurity.dto.PlanActionCardCreate;
import com.sinopec.mmsecurity.dto.PlanActionCardUpdate;
import com.sinopec.mmsecurity.dto.PlanInstance;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.EmergencyPlanService;
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

/** 应急预案大屏接口：矩阵/选项只读，行动卡片支持建改删，数据源为 V18 fac_emergency_plan / fac_plan_* 真实表。 */
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

    /** 在指定预案实例下新建行动卡片。 */
    @PostMapping("/{planId}/action-cards")
    @RequireAuth(role = "ADMIN")
    public Result<PlanActionCard> createActionCard(
            @PathVariable String planId, @Valid @RequestBody PlanActionCardCreate payload) {
        return Result.ok(emergencyPlanService.createActionCard(planId, payload));
    }

    /** 局部更新行动卡片（前端主要用于执行状态流转）。 */
    @PutMapping("/{planId}/action-cards/{cardId}")
    @RequireAuth(role = "ADMIN")
    public Result<PlanActionCard> updateActionCard(
            @PathVariable String planId,
            @PathVariable String cardId,
            @Valid @RequestBody PlanActionCardUpdate payload) {
        return Result.ok(emergencyPlanService.updateActionCard(planId, cardId, payload));
    }

    /** 删除行动卡片。 */
    @DeleteMapping("/{planId}/action-cards/{cardId}")
    @RequireAuth(role = "ADMIN")
    public Result<DeleteResult> deleteActionCard(
            @PathVariable String planId, @PathVariable String cardId) {
        return Result.ok(emergencyPlanService.deleteActionCard(planId, cardId));
    }
}
