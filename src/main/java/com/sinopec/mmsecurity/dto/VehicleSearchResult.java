package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 车辆识别检索结果 DTO —— 与前端 {@code security.openapi.json#/VehicleSearchResult} 字节级对齐。
 * 数据来自真实表 fac_vehicle_search；confidence 可能为空（识别失败）。
 */
@Data
public class VehicleSearchResult {
    private Long id;
    private String plate;
    private Integer confidence;
    private String gate;
    private String status;
    private String time;
}
