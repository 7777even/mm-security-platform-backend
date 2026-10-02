package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急预案目录扁平行（管理端台账编辑用，区别于 /catalog 层次化摘要）。
 * 与前端 {@code emergency-plan.openapi.json#/EmergencyPlanCatalogRow} 对齐。
 */
@Data
public class EmergencyPlanCatalogRow implements Serializable {

    /** 目录行 ID */
    private String id;

    /** 预案层级编码：superior / company / branch / site */
    private String planCode;

    /** 层级标签 */
    private String label;

    /** 当前生效预案名称 */
    private String planName;

    /** 是否可切换（0/1） */
    private Integer canSwitch;

    /** 是否为当前激活预案（0/1） */
    private Integer isCurrent;

    /** 排序号 */
    private Integer sortNo;
}
