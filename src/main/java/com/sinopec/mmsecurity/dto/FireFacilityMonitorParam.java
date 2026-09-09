package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 消防设施监测 - 监控参数（label/value/tone，tone：normal|warning|danger）。 */
@Data
public class FireFacilityMonitorParam implements Serializable {
    private static final long serialVersionUID = 1L;

    private String label;
    private String value;
    private String tone;
}
