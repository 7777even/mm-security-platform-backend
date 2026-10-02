package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.EmergencyPlanCatalogSummary;
import com.sinopec.mmsecurity.dto.EmergencyPlanDetailSummary;
import com.sinopec.mmsecurity.dto.EmergencyPlanOptions;
import com.sinopec.mmsecurity.dto.PlanActionCard;
import com.sinopec.mmsecurity.dto.PlanActionCardCreate;
import com.sinopec.mmsecurity.dto.PlanActionCardUpdate;
import com.sinopec.mmsecurity.dto.PlanInstance;
import com.sinopec.mmsecurity.dto.PlanInvokeRequest;
import com.sinopec.mmsecurity.dto.PlanInvokeResult;
import com.sinopec.mmsecurity.dto.EmergencyPlanCatalogRow;
import com.sinopec.mmsecurity.dto.EmergencyPlanCatalogWriteRequest;
import com.sinopec.mmsecurity.dto.EmergencyPlanMetaItem;
import com.sinopec.mmsecurity.dto.EmergencyPlanMetaWriteRequest;
import com.sinopec.mmsecurity.security.RequireAuth;
import java.util.List;
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

    /** 预案切换面板选项：页签 + 事故类型/装置筛选字典 + 预案目录。domain 非空时仅返回该业务域预案。 */
    @GetMapping("/options")
    public Result<EmergencyPlanOptions> options(
            @RequestParam(required = false) String domain) {
        return Result.ok(emergencyPlanService.options(domain));
    }

    /** 预案目录（4 行层级：上级单位 / 公司级 / 消防救援 / 现场处置）。V39。 */
    @GetMapping("/catalog")
    public Result<EmergencyPlanCatalogSummary> catalog() {
        return Result.ok(emergencyPlanService.planCatalog());
    }

    /** 预案详情字段（5 段：基础 / 评审 / 备案 / 公布 / 评估信息）。V39。 */
    @GetMapping("/catalog-detail")
    public Result<EmergencyPlanDetailSummary> catalogDetail() {
        return Result.ok(emergencyPlanService.planCatalogDetail());
    }

    /** 预案矩阵：按 planId 返回预案实例，缺省或未命中时返回默认预案。 */
    @GetMapping("/matrix")
    public Result<PlanInstance> matrix(@RequestParam(required = false) String planId) {
        return Result.ok(emergencyPlanService.matrix(planId));
    }

    /** 一键调用预案：激活 + 广播 + 留痕（不向任何物理设备下发控制指令，符合零下行控制红线）。 */
    @PostMapping("/{id}/invoke")
    @RequireAuth(role = "ADMIN")
    public Result<PlanInvokeResult> invoke(
            @PathVariable("id") Long id,
            @RequestBody(required = false) PlanInvokeRequest body) {
        return Result.ok(emergencyPlanService.invokePlan(id, body));
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

    /* ==================== 管理端台账：预案目录（扁平台账） ==================== */

    /** 预案目录扁平行列表（管理端编辑用，区别于 /catalog 层次化摘要）。 */
    @GetMapping("/catalog-items")
    public Result<List<EmergencyPlanCatalogRow>> catalogItems() {
        return Result.ok(emergencyPlanService.planCatalogRows());
    }

    /**
     * 新增预案目录行。需权限码 {@code emergency:plan-catalog:write}；成功触发 emergency.plan-catalog 实时广播。
     */
    @PostMapping("/catalog-items")
    @RequireAuth(perm = "emergency:plan-catalog:write")
    public Result<EmergencyPlanCatalogRow> createCatalogItem(@RequestBody EmergencyPlanCatalogWriteRequest payload) {
        return Result.ok(emergencyPlanService.createPlanCatalogRow(payload));
    }

    /**
     * 编辑预案目录行（局部更新）。需权限码 {@code emergency:plan-catalog:write}；成功触发 emergency.plan-catalog 实时广播。
     */
    @PutMapping("/catalog-items/{id}")
    @RequireAuth(perm = "emergency:plan-catalog:write")
    public Result<EmergencyPlanCatalogRow> updateCatalogItem(
            @PathVariable Long id, @RequestBody EmergencyPlanCatalogWriteRequest payload) {
        return Result.ok(emergencyPlanService.updatePlanCatalogRow(id, payload));
    }

    /**
     * 删除预案目录行（物理删除）。需权限码 {@code emergency:plan-catalog:write}；成功触发 emergency.plan-catalog 实时广播。
     */
    @DeleteMapping("/catalog-items/{id}")
    @RequireAuth(perm = "emergency:plan-catalog:write")
    public Result<Void> deleteCatalogItem(@PathVariable Long id) {
        emergencyPlanService.deletePlanCatalogRow(id);
        return Result.ok(null);
    }

    /* ==================== 管理端台账：应急预案主记录 ==================== */

    /** 应急预案主记录列表（管理端编辑用，区别于 /options /matrix 大屏视图）。 */
    @GetMapping
    public Result<List<EmergencyPlanMetaItem>> plans() {
        return Result.ok(emergencyPlanService.planMetaList());
    }

    /**
     * 新增应急预案主记录。需权限码 {@code emergency:plan:write}；成功触发 emergency.plan 实时广播。
     */
    @PostMapping
    @RequireAuth(perm = "emergency:plan:write")
    public Result<EmergencyPlanMetaItem> createPlan(@RequestBody EmergencyPlanMetaWriteRequest payload) {
        return Result.ok(emergencyPlanService.createPlan(payload));
    }

    /**
     * 编辑应急预案主记录（局部更新）。需权限码 {@code emergency:plan:write}；成功触发 emergency.plan 实时广播。
     */
    @PutMapping("/{id}")
    @RequireAuth(perm = "emergency:plan:write")
    public Result<EmergencyPlanMetaItem> updatePlan(
            @PathVariable Long id, @RequestBody EmergencyPlanMetaWriteRequest payload) {
        return Result.ok(emergencyPlanService.updatePlan(id, payload));
    }

    /**
     * 删除应急预案主记录（物理删除）。需权限码 {@code emergency:plan:write}；成功触发 emergency.plan 实时广播。
     */
    @DeleteMapping("/{id}")
    @RequireAuth(perm = "emergency:plan:write")
    public Result<Void> deletePlan(@PathVariable Long id) {
        emergencyPlanService.deletePlan(id);
        return Result.ok(null);
    }
}
