package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应急资源项，与前端 {@code emergency.openapi.json#/EmergencyResource} 对齐。
 * kind 枚举：应急专家/应急物资/救援队伍/装备车辆/应急场所/医疗机构/应急车辆/消防设施。
 */
@Data
public class EmergencyResource implements Serializable {

    /** 资源类别 */
    private String kind;
    /** 数量 */
    private Integer count;
    /** 图标名（Element Plus icon 名） */
    private String icon;
    /** 真实明细预览（仅 ledger 源类别有值：应急专家/物资/车辆/救援队伍），其余类别为 null */
    private List<StrengthItem> items;
}
