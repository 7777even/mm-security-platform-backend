package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 巡更/通行轨迹时间轴条目，契约源：前端 SecurityTrackTimelineItem。 */
@Data
public class SecurityTrackTimelineItem {

    private Long id;
    private String location;
    private String status;
    private String statusTone;
    private String time;
    private String captureHint;
}
