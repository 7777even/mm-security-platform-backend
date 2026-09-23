package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消防设施监测运行数据上报请求：设备/采集/模拟上报的分项列表。
 */
@Data
public class FireFacilityMonitorReportRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 上报的分项列表（按 key upsert）。 */
    private List<FireFacilityMonitorReportItem> items;
}
