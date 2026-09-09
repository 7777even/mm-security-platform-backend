package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 七日天气预报实体（对应 H2 表 fac_weather_daily），共 7 天。
 *
 * <p>wind_text 为带风向风级的展示文案（如 '东南风 2级'），不做拆分。
 */
@Data
@TableName(value = "fac_weather_daily")
public class FacWeatherDaily implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dayLabel;

    private String dateLabel;

    private String conditionText;

    private String iconText;

    private Integer highTemp;

    private Integer lowTemp;

    private String windText;

    private Integer humidityValue;

    private Double rainValue;

    private Integer sortNo;
}
