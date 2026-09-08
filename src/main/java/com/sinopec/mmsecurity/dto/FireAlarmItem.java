package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 消防报警列表项 DTO —— 与前端 {@code fire-alarm.openapi.json#/FireAlarmItem}（及 src/services/alarm.ts 的 FireAlarmItem）字节级对齐。
 * 数据来自真实表 fac_fire_alarm。
 */
@Data
public class FireAlarmItem {
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
}
