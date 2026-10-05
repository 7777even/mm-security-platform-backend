package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

/**
 * 消防报警列表项 DTO —— 与前端 {@code fire-alarm.openapi.json#/FireAlarmItem}（及 src/services/alarm.ts 的 FireAlarmItem）字节级对齐。
 * 数据来自真实表 fac_fire_alarm。
 */
@Data
public class FireAlarmItem implements ZoneAware {
    private String alarmId;
    private String typeLabel;
    private String typeTone;
    private String source;
    private String objectType;
    private String objectName;
    private String level;
    private String description;
    private String location;
    private String time;
    private String falseAlarm;
    private String status;
    private String rescueEventId;
    private String monitorId;
    private String monitorLabel;
    private String onsiteMonitorId;
    private String onsiteMonitorLabel;
    private String title;
    /** 处置情况文本（可空）。 */
    private String handleResult;
    /** 处置时间（格式 yyyy-MM-dd HH:mm:ss，可空）。 */
    private String handleTime;
    /** 派单人员（多个以英文逗号分隔，可空）。 */
    private String dispatchPersonnel;
    /** 通知方式（APP/SMS，多个以英文逗号分隔，可空）。 */
    private String notifyMethod;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（消防报警位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
