package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 防恐柱列表项 DTO —— 与前端 {@code security.openapi.json#/BollardItem} 字节级对齐。
 * 数据来自真实表 fac_bollard。
 */
@Data
public class BollardItem {
    private Long id;
    private String name;
    private String zone;
    private String status;
    private Double longitude;
    private Double latitude;
}
