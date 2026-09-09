package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 应急预案 - 完整预案矩阵（阶段/子阶段/风险事件/作战力量/行动卡片）。 */
@Data
public class PlanInstance {
    private String id;
    private String title;
    private String description;
    private List<PlanMajorPhase> majorPhases;
    private List<PlanSubPhase> subPhases;
    private List<PlanRiskEvent> riskEvents;
    private List<PlanCombatResource> resources;
    private List<PlanActionCard> actionCards;
}
