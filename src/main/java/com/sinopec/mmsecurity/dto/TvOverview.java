package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 工业电视 - 首屏聚合：概览卡片 + 运行统计 + 维保工单 + 事件分析。 */
@Data
public class TvOverview {
    private List<TvOverviewItem> overviewItems;
    private TvOperationStats operationStats;
    private List<TvMaintenanceOrder> maintenanceOrders;
    private List<TvEventBreakdownItem> eventBreakdown;
}
