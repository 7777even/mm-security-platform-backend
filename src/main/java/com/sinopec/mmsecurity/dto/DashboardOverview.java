package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 态势总览 DTO —— 与前端脚手架 {@code dashboard.openapi.json#/DashboardOverview} 字节级对齐。
 *
 * 字段：activeAlarm / deviceOnline / deviceTotal / riskIndex / onlineWorkstation / ts。
 * 数据来自真实聚合（fac_device / fac_alarm 计数），不再返回硬编码/随机值。
 */
@Data
public class DashboardOverview {
    private long activeAlarm;
    private long deviceOnline;
    private long deviceTotal;
    private double riskIndex;
    private int onlineWorkstation;
    private LocalDateTime ts;
}
