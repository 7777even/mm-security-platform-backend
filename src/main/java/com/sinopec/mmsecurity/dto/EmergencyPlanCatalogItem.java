package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 应急预案目录行（对应 fac_emergency_plan_catalog，V39）。 */
@Data
public class EmergencyPlanCatalogItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 预案层级编码：superior / company / branch / site */
    private String id;

    /** 层级标签 */
    private String label;

    /** 当前生效预案名称 */
    private String planName;

    /** 是否可切换 */
    private Boolean canSwitch;

    /** 是否为当前激活预案 */
    private Boolean isCurrent;
}
