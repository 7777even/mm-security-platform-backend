package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 报警趋势单点 DTO —— 与前端脚手架 {@code dashboard.openapi.json#/AlarmTrendPoint} 字节级对齐。
 *
 * 字段：hour（"08:00"，小时起点标签）/ count（该小时报警数）。由 {@code DashboardService.trend24h}
 * 按近 24 小时分 24 桶生成；无报警的小时 count=0，保证前端拿到完整 24 点序列。
 */
@Data
public class AlarmTrendPoint {
    private String hour;
    private int count;
}
