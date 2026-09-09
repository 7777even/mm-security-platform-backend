package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 风险预警项（对应前端 RiskWarningItem）。level 取 red / orange / yellow，按红橙黄三级着色。 */
@Data
public class RiskWarningItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String location;
    private String type;
    private String time;
    private String person;
    private String phone;
    private String level;
    private String levelLabel;
}
