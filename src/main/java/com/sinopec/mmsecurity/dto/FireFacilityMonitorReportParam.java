package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施监测运行数据上报 - 监控参数项（label/value/tone，与 fac_fire_facility_param 对齐）。
 */
@Data
public class FireFacilityMonitorReportParam implements Serializable {
    private static final long serialVersionUID = 1L;

    private String label;
    private String value;
    private String tone;
}
