package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 应急预案目录集合（GET /api/v1/emergency-plans/catalog）。 */
@Data
public class EmergencyPlanCatalogSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<EmergencyPlanCatalogItem> items;
}
