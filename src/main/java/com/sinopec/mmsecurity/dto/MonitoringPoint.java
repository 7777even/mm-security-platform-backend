package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 监测点位 DTO —— 与前端 {@code hazard.openapi.json#/MonitoringPoint} 字节级对齐。
 * 数据来自 fac_monitoring_point 真实表。
 */
@Data
public class MonitoringPoint {
    private String id;
    private String name;
    private String category;
    private String status;
    private String lastTime;
    private String org;
    private Double longitude;
    private Double latitude;
}
