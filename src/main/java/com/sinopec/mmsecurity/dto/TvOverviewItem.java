package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 工业电视 - 视频概览卡片项（对应前端 VideoOverviewItem）。 */
@Data
public class TvOverviewItem {
    private Long id;
    private String label;
    private Integer value;
    private Integer iconIndex;
    /**
     * 分类 code：重大危险源=MAJOR_HAZARD；其余类=PRODUCTION/BOUNDARY/CLOSED_GATE/OTHER_GATE/OTHER。
     * 前端据此下钻到对应分类的真实监控点位列表（fac_tv_monitor.monitor_category）。空表示无下钻。
     */
    private String category;
}
