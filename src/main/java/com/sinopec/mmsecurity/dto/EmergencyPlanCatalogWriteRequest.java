package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急预案目录行写请求（新增 / 编辑共用）。字段名对齐 {@link EmergencyPlanCatalogRow}。
 */
@Data
public class EmergencyPlanCatalogWriteRequest implements Serializable {

    /** 预案层级编码（可选）。 */
    private String planCode;

    /** 层级标签（必填）。 */
    private String label;

    /** 当前生效预案名称（可选）。 */
    private String planName;

    /** 是否可切换（0/1，可选）。 */
    private Integer canSwitch;

    /** 是否为当前激活预案（0/1，可选）。 */
    private Integer isCurrent;

    /** 排序号（可选）。 */
    private Integer sortNo;
}
