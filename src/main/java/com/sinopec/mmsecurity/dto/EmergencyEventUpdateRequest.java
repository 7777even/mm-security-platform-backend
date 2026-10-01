package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 编辑应急事件入参（对齐前端 emergency-event.openapi.json#/EmergencyEventUpdateRequest）。
 *
 * <p>与 create 的区别：本请求是<b>局部更新</b>——字段为 {@code null} 表示「不修改该字段」，
 * 便于前端只提交表单里真正改动的项（read-modify-write 由 service 承担）。
 * 事件主键、scene/kind/eventCategory 等分组维度字段不在本请求内，避免把事件挪到
 * 另一个侧栏分组后产生孤儿分组或大屏分组口径漂移。</p>
 *
 * <p>status 与 statusLabel 的联动：只传 status 时由 service 按枚举推导中文标签；
 * 两者都传时以传入为准（前端可自定义态势文案）。status 取值非法走 B3 包络（code != 0）。</p>
 */
@Data
public class EmergencyEventUpdateRequest {

    /** 事件标题 */
    private String title;

    /** 事件位置（装置/区域） */
    private String location;

    /** 事件描述 */
    private String description;

    /** 事件发生时间，格式 yyyy-MM-dd HH:mm:ss */
    private String eventTime;

    /** 事件状态：pending 未处置 / processing 处置中 / done 已处置 */
    private String status;

    /** 状态中文标签（可选，缺省随 status 推导） */
    private String statusLabel;

    /** 是否已预警（报送） */
    private Boolean reported;

    /** 所属区域编码 */
    private String areaCode;

    /** 危险源等级 */
    private String hazardSourceLevel;

    /** 地图撒点左偏移（百分比，如 48.3%） */
    private String leftPercent;

    /** 地图撒点上偏移（百分比，如 36.1%） */
    private String topPercent;

    /** 经度 */
    private Double longitude;

    /** 纬度 */
    private Double latitude;

    /** 结束时间，格式 yyyy-MM-dd HH:mm:ss */
    private String endedAt;
}
