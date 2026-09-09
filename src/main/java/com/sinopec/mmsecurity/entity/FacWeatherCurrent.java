package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 天气实况实体（对应 H2 表 fac_weather_current），设计上仅一行。
 *
 * <p>带量纲的展示文案（如 '2.4m/s'、'76%'、'1004hPa'）原样以 VARCHAR 入库，
 * 服务层不做单位换算；列名统一回避 H2 保留字（condition→condition_text）。
 */
@Data
@TableName(value = "fac_weather_current")
public class FacWeatherCurrent implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer temperature;

    private String conditionText;

    private Integer airQuality;

    private String airQualityLevel;

    private String windDirection;

    private String windSpeed;

    private String windLevel;

    private String humidityText;

    private String pressureText;

    private String visibilityText;

    private String rainfallText;

    private String updatedAt;
}
