package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 应急预案 - 切换面板选项：页签 + 事故类型/装置筛选字典 + 预案目录。 */
@Data
public class EmergencyPlanOptions {
    private List<EmergencyPlanTab> tabs;
    private List<String> accidentTypes;
    private List<String> facilities;
    private List<SelectableEmergencyPlan> plans;
}
