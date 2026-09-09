package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 工业电视 - 事件分析构成项（对应前端 EventBreakdownItem，color 为前端固定配色）。 */
@Data
public class TvEventBreakdownItem {
    private String label;
    private Integer value;
    private String color;
}
