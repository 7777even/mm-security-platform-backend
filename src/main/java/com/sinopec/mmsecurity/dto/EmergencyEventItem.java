package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

/** 应急事件条目（与前端 EmergencyEventItem 契约一致）。 */
@Data
public class EmergencyEventItem implements ZoneAware {

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

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（事件发生位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
