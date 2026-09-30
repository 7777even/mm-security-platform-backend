package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 工业电视维修工单真实台账（V88 新建）。
 *
 * <p>取代 fac_tv_stat_item 中 MAINTENANCE 字典手填值（未接单/处理中/已超时 12/25/8），使概览卡片
 * 的工单计数由本表 GROUP BY order_status 实时统计，并支持按状态下钻真实工单明细。
 *
 * <p>order_status 取值：PENDING 未接单 / PROCESSING 处理中 / OVERTIME 已超时。
 * 列名 order_status 回避 SQL 保留字 status（见 V15 命名约定）。
 */
@Data
@TableName("fac_tv_maintenance_order")
public class FacTvMaintenanceOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单编号（如 WO-2026-0901） */
    private String orderNo;

    /** 设备/点位名称 */
    private String deviceName;

    /** 设备编码（可选） */
    private String deviceCode;

    /** 故障描述 */
    private String faultDesc;

    /** 工单状态 code：PENDING/PROCESSING/OVERTIME */
    @TableField("order_status")
    private String status;

    /** 派单人/负责人（未接单为空） */
    private String assignee;

    /** 责任部门 */
    private String department;

    /** 防区编码（关联 sys_zone.zone_code，可选） */
    private String zoneCode;

    /** 创建时间 yyyy-MM-dd HH:mm:ss */
    private String createdAt;

    /** 计划完成时间（可选） */
    private String planFinishTime;

    /** 实际完成时间（进行中/已超时为空） */
    private String actualFinishTime;

    /** 处理说明（可选） */
    private String handleDesc;

    private Integer sortNo;
}
