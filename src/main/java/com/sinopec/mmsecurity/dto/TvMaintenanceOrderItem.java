package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 工业电视维修工单明细项（按状态下钻真实工单）。与前端
 * {@code tv.openapi.json#/components/schemas/TvMaintenanceOrderItem} 对齐。
 */
@Data
public class TvMaintenanceOrderItem implements Serializable {

    private Long id;
    /** 工单编号 */
    private String orderNo;
    /** 设备/点位名称 */
    private String deviceName;
    /** 设备编码 */
    private String deviceCode;
    /** 故障描述 */
    private String faultDesc;
    /** 工单状态 code：PENDING/PROCESSING/OVERTIME */
    private String status;
    /** 工单状态中文（未接单/处理中/已超时） */
    private String statusLabel;
    /** 派单人/负责人 */
    private String assignee;
    /** 责任部门 */
    private String department;
    /** 防区编码 */
    private String zoneCode;
    /** 创建时间 */
    private String createdAt;
    /** 计划完成时间 */
    private String planFinishTime;
    /** 实际完成时间 */
    private String actualFinishTime;
    /** 处理说明 */
    private String handleDesc;
}
