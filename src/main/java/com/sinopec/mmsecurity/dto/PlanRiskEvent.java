package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急预案 - 子阶段关联的风险事件。 */
@Data
public class PlanRiskEvent {
    private String id;
    private String subPhaseId;
    private String name;
}
