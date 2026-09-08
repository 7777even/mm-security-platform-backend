package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 巡逻摄像机列表项 DTO —— 与前端 {@code security.openapi.json#/PatrolCameraItem} 字节级对齐。
 * 数据来自真实表 fac_patrol_camera。
 */
@Data
public class PatrolCameraItem {
    private Long id;
    private String name;
    private String zone;
    private String status;
    private Double longitude;
    private Double latitude;
}
