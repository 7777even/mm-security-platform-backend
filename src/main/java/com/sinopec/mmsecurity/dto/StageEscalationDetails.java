package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 升级规则现场概况，与前端 {@code escalationRule.details} 对齐（契约 #/StageEscalationDetails）。 */
@Data
public class StageEscalationDetails implements Serializable {

    /** 事发位置 */
    private String location;

    /** 涉及介质 */
    private String substance;

    /** 伤员情况 */
    private String casualty;

    /** 当前处置状态 */
    private String currentStatus;
}
