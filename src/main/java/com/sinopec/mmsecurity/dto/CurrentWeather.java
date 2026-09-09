package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 天气 - 实况。
 *
 * <p>除温度与空气质量指数外，其余均为带量纲的展示字符串（如 '2.4m/s'、'76%'、'1004hPa'），
 * 与前端 weatherMock.ts 保持原样一致，后端不做单位换算。
 */
@Data
public class CurrentWeather {
    private Integer temperature;
    private String condition;
    private Integer airQuality;
    private String airQualityLevel;
    private String windDirection;
    private String windSpeed;
    private String windLevel;
    private String humidity;
    private String pressure;
    private String visibility;
    private String rainfall;
    private String updatedAt;
}
