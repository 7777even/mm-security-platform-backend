package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 天气 - 逐小时序列项（8 个点位）。 */
@Data
public class HourlyWeatherItem {
    private String time;
    private Double rain;
    private Double wind;
    private Integer temperature;
    private Double pressure;
    private Integer humidity;
}
