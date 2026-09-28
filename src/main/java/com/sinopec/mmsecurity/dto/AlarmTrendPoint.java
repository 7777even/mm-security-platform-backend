package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 报警趋势单点 DTO —— 与前端脚手架 {@code dashboard.openapi.json#/AlarmTrendPoint} 字节级对齐。
 *
 * 字段：date（"09-22"，日期标签）/ count（当天报警总数）。由 {@code DashboardService.trendDaily}
 * 按近 7 天（含今天）每天聚合生成 7 个桶；无报警的日子 count=0，保证前端拿到完整 7 点序列。
 * 今天刚新增的报警即时计入「今天」那个桶。
 */
@Data
public class AlarmTrendPoint {
    private String date;
    private int count;
}
