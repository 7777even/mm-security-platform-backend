package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 风险等级汇总计数（对应前端 RiskSummary）。
 * 不单独建表，由 fac_production_risk_warning 按 level_code 聚合得出。
 */
@Data
public class RiskSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer red;
    private Integer orange;
    private Integer yellow;
}
