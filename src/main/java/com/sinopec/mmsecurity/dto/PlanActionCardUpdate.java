package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 更新预案行动卡入参（局部更新，null 字段不覆盖），与前端 {@code emergency-plan.openapi.json#/PlanActionCardUpdate} 对齐。
 *
 * <p>前端主要用 status 做行动卡片执行状态流转（pending/in-progress/completed），
 * 其余字段为可选的编辑能力。</p>
 */
@Data
public class PlanActionCardUpdate {

    /** 关联作战资源 id（可选） */
    private String resourceId;

    /** 卡片标题（可选） */
    private String title;

    /** 卡片内容（可选） */
    private String content;

    /** 卡片描述（可选） */
    private String description;

    /** 起始子阶段 id（可选） */
    private String startSubPhaseId;

    /** 结束子阶段 id（可选） */
    private String endSubPhaseId;

    /** 关联风险事件 id（可选） */
    private String riskEventId;

    /** 状态：pending / in-progress / completed（可选） */
    private String status;

    /** 是否跨阶段全局卡（可选） */
    private Boolean isGlobal;
}
