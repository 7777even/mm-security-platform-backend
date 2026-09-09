package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急事件条目（与前端 EmergencyEventItem 契约一致）。 */
@Data
public class EmergencyEventItem {

    private Long id;

    private String areaCode;

    private String title;

    private String location;

    private String description;

    private String time;

    private Boolean reported;

    private String status;

    private String statusLabel;

    /** 设计稿舞台百分比字符串，如 "47.1%"。 */
    private String left;

    /** 设计稿舞台百分比字符串，如 "22.6%"。 */
    private String top;

    private Double longitude;

    private Double latitude;

    /** EVENT=应急事件 / DRILL=应急演练。 */
    private String kind;

    /** default=事故详情 / extremeWeather=极端天气专用详情页。 */
    private String eventCategory;

    private String hazardSourceLevel;

    private String endedAt;

    /** 仅极端天气事件有值，其余为 null（Jackson 默认输出 null，前端已处理）。 */
    private EmergencyEventWeatherMeta weatherMeta;
}
