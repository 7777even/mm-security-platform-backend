package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 设施档案明细 DTO —— 与前端 {@code hazard.openapi.json#/FacilityDetailInfo} 字节级对齐。
 * basicFields/chemicalFields/archives 以 List&lt;Map&gt; 承载，由 fac_facility_detail 的 JSON 列解析而来。
 */
@Data
public class FacilityDetailInfo {
    private String facilityName;
    private String hazardSourceCode;
    private List<Map<String, Object>> basicFields;
    private List<Map<String, Object>> chemicalFields;
    private List<Map<String, Object>> archives;
}
