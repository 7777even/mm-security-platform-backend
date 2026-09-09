package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 逐小时天气序列实体（对应 H2 表 fac_weather_hourly），共 8 个点位。
 *
 * <p>time_label 为展示文案（'当前' / '12:00'），数值列统一带 _value 后缀以避免保留字。
 */
@Data
@TableName(value = "fac_weather_hourly")
public class FacWeatherHourly implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String timeLabel;

    private Double rainValue;

    private Double windValue;

    private Integer temperature;

    private Double pressureValue;

    private Integer humidityValue;

    private Integer sortNo;
}
