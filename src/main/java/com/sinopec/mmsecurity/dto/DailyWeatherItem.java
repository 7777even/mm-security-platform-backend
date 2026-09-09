package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 天气 - 七日预报项。icon 为表情符号字符，直接透传给前端渲染。 */
@Data
public class DailyWeatherItem {
    private String day;
    private String date;
    private String condition;
    private String icon;
    private Integer high;
    private Integer low;
    private String wind;
    private Integer humidity;
    private Double rain;
}
