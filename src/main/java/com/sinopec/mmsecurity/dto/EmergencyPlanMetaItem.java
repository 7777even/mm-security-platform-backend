package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急预案主记录条目（管理端台账编辑用，区别于 /options /matrix 大屏视图）。
 * 与前端 {@code emergency-plan.openapi.json#/EmergencyPlanMetaItem} 对齐。
 */
@Data
public class EmergencyPlanMetaItem implements Serializable {

    /** 预案 ID */
    private String id;

    /** 页签 key：disposal / fire / company / superior */
    private String tabKey;

    /** 预案名称 */
    private String planName;

    /** 事故类型 */
    private String accidentType;

    /** 装置 */
    private String facility;

    /** 业务域：production / fire / perimeter / superior */
    private String domain;

    /** 核预案标记（0/1） */
    private Boolean nuclear;

    /** 当前是否激活（0/1） */
    private Boolean isActive;

    /** 累计一键调用次数 */
    private Integer invokeCount;

    /** 最近一次调用时间（yyyy-MM-dd HH:mm:ss） */
    private String lastInvokedAt;
}
