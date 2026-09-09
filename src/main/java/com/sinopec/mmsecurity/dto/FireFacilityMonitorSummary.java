package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 分类监控卡片（总数/在线/离线/故障 + 监控参数 + 最近上报时间）。 */
@Data
public class FireFacilityMonitorSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private String key;
    private String facilityType;
    private Integer total;
    private Integer online;
    private Integer offline;
    private Integer fault;
    private String status;
    private List<FireFacilityMonitorParam> params;
    private String lastReportTime;
}
