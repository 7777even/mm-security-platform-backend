package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新建预案行动卡入参，与前端 {@code emergency-plan.openapi.json#/PlanActionCardCreate} 对齐。
 *
 * <p>resourceId/title/startSubPhaseId/endSubPhaseId 为必填（对应 fac_plan_action_card 非空列）；
 * status 可空，缺省后端置 pending；isGlobal 可空，缺省 FALSE。</p>
 */
@Data
public class PlanActionCardCreate {

    /** 关联作战资源 id */
    @NotBlank(message = "resourceId 必填")
    private String resourceId;

    /** 卡片标题 */
    @NotBlank(message = "title 必填")
    private String title;

    /** 卡片内容（可选） */
    private String content;

    /** 卡片描述（可选） */
    private String description;

    /** 起始子阶段 id */
    @NotBlank(message = "startSubPhaseId 必填")
    private String startSubPhaseId;

    /** 结束子阶段 id */
    @NotBlank(message = "endSubPhaseId 必填")
    private String endSubPhaseId;

    /** 关联风险事件 id（可选） */
    private String riskEventId;

    /** 状态：pending 待执行 / in-progress 执行中 / completed 已完成；可空，缺省 pending */
    private String status;

    /** 是否跨阶段全局卡；可空，缺省 FALSE */
    private Boolean isGlobal;
}
