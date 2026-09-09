package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 辅助统计项 */
@Data
public class RescueAuxiliaryStat implements Serializable {
    private static final long serialVersionUID = 1L;

    private String label;
    private Integer value;
    private Integer iconIndex;
}
