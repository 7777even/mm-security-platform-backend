package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 天气大屏首屏聚合：实况 + 逐小时序列 + 七日预报。 */
@Data
public class WeatherOverview {
    private CurrentWeather current;
    private List<HourlyWeatherItem> hourly;
    private List<DailyWeatherItem> daily;
}
