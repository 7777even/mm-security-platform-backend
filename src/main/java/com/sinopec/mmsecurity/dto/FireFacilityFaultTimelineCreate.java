package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消防故障时间线新增条目（写回时随状态流转一并追加）。
 * 字段对齐 fac_fire_facility_fault_timeline：eventTime/operatorName/actionName/detailText。
 */
@Data
public class FireFacilityFaultTimelineCreate implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 事件时间（格式 yyyy-MM-dd HH:mm:ss）。 */
    private String time;

    /** 操作人。 */
    private String operator;

    /** 动作名（如 确认故障 / 生成工单并派发 / 开始维修 / 提交验收 / 验收合格 / 验收不合格）。 */
    private String action;

    /** 说明。 */
    private String detail;
}
