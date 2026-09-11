package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 应急预案详情字段分组段（如「基础信息」含若干字段）。 */
@Data
public class EmergencyPlanDetailSection implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private List<EmergencyPlanDetailField> fields;
}
