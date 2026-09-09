package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 统计概览项（对应前端 StatOverviewItem）。unit 可为空，trendUp 决定前端涨绿跌红的配色。 */
@Data
public class StatOverviewItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String label;
    private String value;
    private String unit;
    private Double trend;
    private Boolean trendUp;
    private Integer iconIndex;
}
