package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急预案 - 行动卡片；status 取 pending / in-progress / completed。 */
@Data
public class PlanActionCard {
    private String id;
    private String resourceId;
    private String title;
    private String content;
    private String description;
    private String startSubPhaseId;
    private String endSubPhaseId;
    private String riskEventId;
    private String status;
    private Boolean isGlobal;
}
