package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消防救援力量分项统计，与前端
 * {@code fire-monitoring.openapi.json#/components/schemas/RescueForceStat} 对齐。
 */
@Data
public class RescueForceStat implements Serializable {

    /** 统计项名称（消防队伍 / 救援人员 / 救援装备 / 救援车辆） */
    private String label;
    /** 数量 */
    private Integer value;
    /** 计量单位（支 / 人 / 套 / 台） */
    private String unit;
    /** 图标类型（squad 队伍 / person 人员 / vehicle 车辆 / equipment 装备） */
    private String iconType;
}
