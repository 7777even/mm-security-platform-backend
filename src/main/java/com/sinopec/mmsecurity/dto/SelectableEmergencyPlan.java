package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急预案 - 可选预案条目（切换面板目录行）。 */
@Data
public class SelectableEmergencyPlan {
    private String id;
    private String tab;
    private String name;
    private String accidentType;
    private String facility;
}
