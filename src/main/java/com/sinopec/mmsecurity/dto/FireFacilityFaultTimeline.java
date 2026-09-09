package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 消防设施监测 - 故障时间线条目（time/operator/action/detail）。 */
@Data
public class FireFacilityFaultTimeline implements Serializable {
    private static final long serialVersionUID = 1L;

    private String time;
    private String operator;
    private String action;
    private String detail;
}
