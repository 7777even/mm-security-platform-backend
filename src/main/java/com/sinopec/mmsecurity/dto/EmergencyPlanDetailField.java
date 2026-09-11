package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 应急预案详情字段（标签 + 值）。 */
@Data
public class EmergencyPlanDetailField implements Serializable {
    private static final long serialVersionUID = 1L;

    private String label;
    private String value;
}
