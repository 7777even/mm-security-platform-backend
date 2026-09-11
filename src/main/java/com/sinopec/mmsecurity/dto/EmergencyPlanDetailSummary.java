package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 应急预案详情字段集合（GET /api/v1/emergency-plans/catalog-detail）。 */
@Data
public class EmergencyPlanDetailSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<EmergencyPlanDetailSection> sections;
}
