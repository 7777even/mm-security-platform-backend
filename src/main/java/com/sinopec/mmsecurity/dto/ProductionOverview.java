package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 生产应急大屏首屏总览（对应前端 ProductionOverview）。
 * 一次返回设施卡片、设备分类卡片、统计概览条与风险汇总，避免首屏多端点拼接。
 */
@Data
public class ProductionOverview implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<OverviewGridItem> facilities = new ArrayList<>();
    private List<OverviewGridItem> devices = new ArrayList<>();
    private List<StatOverviewItem> stats = new ArrayList<>();
    private RiskSummary riskSummary;
}
