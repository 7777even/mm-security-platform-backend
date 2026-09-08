package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 监测告警 DTO —— 与前端 {@code hazard.openapi.json#/MonitoringAlarm} 字节级对齐。
 * 数据来自 fac_monitoring_alarm 真实表。
 */
@Data
public class MonitoringAlarm {
    private String id;
    private String title;
    private String detail;
    private String area;
    private String time;
    private String level;
}
