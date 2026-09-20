package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增应急事件入参（对齐前端 emergency-event.openapi.json#/EmergencyEventCreateRequest）。
 *
 * <p>scene 由前端传入（FIRE 消防应急 / PRELIMINARY 先期处置）；kind/eventCategory 控制分组与明细页；
 * 无法单独映射的字段（上报人/电话/伤亡数/事件类型细分）由前端并入 description。
 * service 同事务写入 fac_emergency_event 与 fac_accident_incident（is_default=false），
 * 使「去处置」页面能按 event_id 定位到该事件，而非回退到默认事件。</p>
 */
@Data
public class EmergencyEventCreateRequest {

    /** 事件场景：FIRE 消防应急 / PRELIMINARY 先期处置 */
    @NotBlank(message = "scene 必填")
    private String scene;

    /** 事件类型：event 真实事件 / drill 演练（前端传小写，service 转大写落库） */
    @NotBlank(message = "kind 必填")
    private String kind;

    /** 事件分类：default 默认 / extremeWeather 极端天气 */
    @NotBlank(message = "eventCategory 必填")
    private String eventCategory;

    /** 事件标题 */
    @NotBlank(message = "title 必填")
    private String title;

    /** 事件位置（装置/区域） */
    @NotBlank(message = "location 必填")
    private String location;

    /** 事件描述（含上报人/电话/伤亡数等无法单独映射的信息） */
    @NotBlank(message = "description 必填")
    private String description;

    /** 事件发生时间，格式 yyyy-MM-dd HH:mm:ss */
    @NotBlank(message = "eventTime 必填")
    private String eventTime;

    /** 所属区域编码（可选，缺省 refinery） */
    private String areaCode;

    /** 危险源等级（可选） */
    private String hazardSourceLevel;

    /** 地图撒点左偏移（百分比，如 48.3%） */
    @NotBlank(message = "leftPercent 必填")
    private String leftPercent;

    /** 地图撒点上偏移（百分比，如 36.1%） */
    @NotBlank(message = "topPercent 必填")
    private String topPercent;

    /** 经度 */
    @NotNull(message = "longitude 必填")
    private Double longitude;

    /** 纬度 */
    @NotNull(message = "latitude 必填")
    private Double latitude;

    /** 极端天气类型（eventCategory=extremeWeather 时填） */
    private String weatherType;

    /** 预警等级（极端天气时填） */
    private String warningLevel;

    /** 影响区域（极端天气时填） */
    private String affectedArea;

    /** 监测时段（极端天气时填） */
    private String monitoringPeriod;

    /** 数据来源（极端天气时填） */
    private String weatherSource;

    /** 应对措施（极端天气时填） */
    private String measures;
}
