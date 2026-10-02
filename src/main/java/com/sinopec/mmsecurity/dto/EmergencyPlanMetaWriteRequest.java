package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急预案主记录写请求（新增 / 编辑共用）。字段名对齐 {@link EmergencyPlanMetaItem}。
 */
@Data
public class EmergencyPlanMetaWriteRequest implements Serializable {

    /** 预案名称（必填）。 */
    private String planName;

    /** 预案类别 Tab 键：disposal / fire / company / superior（可选）。 */
    private String tabKey;

    /** 事故类型（可选）。 */
    private String accidentType;

    /** 装置（可选）。 */
    private String facility;

    /** 业务域（可选）。 */
    private String domain;

    /** 核预案标记（0/1，可选）。 */
    private Boolean nuclear;

    /** 当前是否激活（0/1，可选）。 */
    private Boolean isActive;

    /** 排序号（可选）。 */
    private Integer sortNo;
}
