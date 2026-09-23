package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 周界入侵告警创建请求：手工录入。title 必填，其余可缺省由后端填充默认值。
 * <p>字段名与前端契约 {@code PerimeterAlarmCreateRequest} schema 同名，守门脚本按同名类逐字段对拍。
 * <ul>
 *   <li>title：告警标题（必填，后端校验非空）</li>
 *   <li>alarmType：告警类型（缺省 周界入侵告警）</li>
 *   <li>levelCode：告警等级</li>
 *   <li>location：告警位置</li>
 *   <li>alarmTime：发生时间 yyyy-MM-dd HH:mm:ss（缺省当前时刻）</li>
 *   <li>description / objectName / objectType / intrusionPosition / intrusionMethod / relatedCamera：可选</li>
 * </ul>
 */
@Data
public class PerimeterAlarmCreateRequest {
    /** 告警标题（必填）。 */
    private String title;
    /** 告警类型（缺省 周界入侵告警）。 */
    private String alarmType;
    /** 告警等级。 */
    private String levelCode;
    /** 告警位置。 */
    private String location;
    /** 发生时间（yyyy-MM-dd HH:mm:ss，缺省当前时刻）。 */
    private String alarmTime;
    /** 告警说明。 */
    private String description;
    /** 入侵对象名称（可选）。 */
    private String objectName;
    /** 入侵对象类型（可选）。 */
    private String objectType;
    /** 入侵位置（可选）。 */
    private String intrusionPosition;
    /** 入侵方式（可选）。 */
    private String intrusionMethod;
    /** 关联摄像机（可选）。 */
    private String relatedCamera;
}
