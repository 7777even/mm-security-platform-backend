package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 极端天气事件的扩展元信息（与前端 EmergencyEventItem.weatherMeta 契约一致）。 */
@Data
public class EmergencyEventWeatherMeta {

    private String weatherType;

    private String warningLevel;

    private String affectedArea;

    private String monitoringPeriod;

    private String source;

    private String measures;
}
