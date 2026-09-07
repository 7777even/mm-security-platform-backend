package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 风险热力图分区评分项，与前端 {@code dashboard.openapi.json#/RiskHeatItem} 对齐。
 */
@Data
public class RiskHeatItem implements Serializable {

    /** 区域名（罐区/装置区/装卸区/公用工程/行政办公 等，取设备主数据实际 zone） */
    private String zone;

    /** 风险评分（真实聚合，非随机） */
    private Double score;
}
