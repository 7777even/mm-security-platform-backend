package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 重大危险源明细 DTO —— 与前端 {@code hazard.openapi.json#/MajorHazardDetail} 字节级对齐。
 * 嵌套明细（contacts/files/monitors/videos/chemicals/evacuationRoutes/operations）以 List&lt;Map&gt; 承载，
 * 由 FacMajorHazard 的 JSON 列解析而来。
 */
@Data
public class MajorHazardDetail {
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
    private String commissionDate;
    private Boolean keyProcess;
    private Boolean inChemicalPark;
    private List<Map<String, Object>> contacts;
    private List<Map<String, Object>> files;
    private List<Map<String, Object>> monitors;
    private List<Map<String, Object>> videos;
    private List<Map<String, Object>> chemicals;
    private List<Map<String, Object>> evacuationRoutes;
    private List<Map<String, Object>> operations;
}
