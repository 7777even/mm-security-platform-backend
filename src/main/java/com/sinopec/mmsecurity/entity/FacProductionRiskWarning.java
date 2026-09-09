package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产应急 - ProductionRiskWarning 实体（风险预警，对应 H2 表 fac_production_risk_warning）。
 * level_code（red/orange/yellow）同时用于总览 riskSummary 的聚合计数。
 */
@Data
@TableName(value = "fac_production_risk_warning")
public class FacProductionRiskWarning implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String location;

    private String typeName;

    private String occurredAt;

    private String person;

    private String phone;

    private String levelCode;

    private String levelLabel;

    private Integer sortNo;
}
