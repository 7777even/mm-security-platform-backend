package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 监控端点响应（设施类型下拉 + 监控卡片列表）。 */
@Data
public class FireFacilityMonitorResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> typeOptions;
    private List<FireFacilityMonitorSummary> items;
}
