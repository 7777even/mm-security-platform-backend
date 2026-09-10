package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 应急流程节点，与前端 {@code ProcessStage} 对齐（契约 emergency.openapi.json#/ProcessStage）。 */
@Data
public class ProcessStage implements Serializable {

    /** 节点号（1-15） */
    private Integer id;

    /** 节点名称 */
    private String name;

    /** 节点简称（列表/流程条用） */
    private String shortName;

    /** 牵头角色（含图标） */
    private String leadRole;

    /** 牵头岗位 */
    private String leadTitle;

    /** 指挥层级 */
    private String commandLevel;

    /** 节点说明 */
    private String description;

    /** 前置态势（上一节点以来的关键时间线） */
    private List<String> previousContext;

    /** 当前处置动作清单 */
    private List<ProcessAction> currentActions;

    /** 完成判据清单 */
    private List<CriteriaChecklistItem> criteriaChecklist;

    /** 子阶段清单（装置区等节点使用） */
    private List<SubStageItem> subStages;

    /** 升级规则 */
    private StageEscalationRule escalationRule;
}
