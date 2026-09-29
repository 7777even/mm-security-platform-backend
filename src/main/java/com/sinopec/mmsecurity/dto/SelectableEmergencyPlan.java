package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 应急预案 - 可选预案条目（切换面板目录行）。 */
@Data
public class SelectableEmergencyPlan {
    private String id;
    private String tab;
    private String name;
    private String accidentType;
    private String facility;
    /** 业务域：production / fire / perimeter / superior。 */
    private String domain;
    /** 核预案标记。 */
    private Boolean nuclear;
    /** 当前是否激活。 */
    private Boolean isActive;
    /** 累计一键调用次数。 */
    private Integer invokeCount;
    /** 最近一次调用时间。 */
    private LocalDateTime lastInvokedAt;
}
