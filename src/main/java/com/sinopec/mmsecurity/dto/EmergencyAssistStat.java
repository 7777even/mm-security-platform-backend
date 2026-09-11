package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 应急辅助信息统计项（对应 fac_emergency_assist_stat，V39）。 */
@Data
public class EmergencyAssistStat implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 统计项标签 */
    private String label;

    /** 数值（如 15） */
    private Integer value;

    /** 单位：套 / 张 / 人 / 处 */
    private String unit;

    /** 配色：blue / cyan / green / orange */
    private String tone;
}
