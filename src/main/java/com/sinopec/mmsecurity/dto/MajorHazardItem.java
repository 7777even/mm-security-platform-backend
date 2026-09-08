package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 重大危险源列表项 DTO —— 与前端 {@code hazard.openapi.json#/MajorHazardItem} 字节级对齐。
 * 数据来自真实主数据表 fac_major_hazard（由 FacMajorHazardMapper 查询），不返回硬编码/随机值。
 */
@Data
public class MajorHazardItem {
    private Long id;
    private String name;
    private String level;
    private Double rValue;
    private Integer monitorCount;
    private Integer videoCount;
    private String enterprise;
    private String category;
    private String code;
    private Double longitude;
    private Double latitude;
}
