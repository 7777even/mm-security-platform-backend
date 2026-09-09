package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 工业电视 - 视频概览卡片项（对应前端 VideoOverviewItem）。 */
@Data
public class TvOverviewItem {
    private Long id;
    private String label;
    private Integer value;
    private Integer iconIndex;
}
