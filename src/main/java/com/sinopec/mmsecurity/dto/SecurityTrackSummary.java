package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 轨迹概要（起点/终点标签 + 时间范围），契约源：前端 SecurityTrackSummary。 */
@Data
public class SecurityTrackSummary {

    private String startLabel;
    private String endLabel;
    private String timeRange;
}
