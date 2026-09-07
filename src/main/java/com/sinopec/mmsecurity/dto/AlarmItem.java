package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警/应急事件对外 DTO —— 与前端脚手架 {@code alarm.openapi.json#/AlarmItem} 字节级对齐。
 *
 * 字段命名/类型严格遵循前端契约：alarmId(string) / status(string 枚举) / ts(date-time) /
 * description / location / category / warned / planId 等。由 {@code AlarmAssembler}
 * 从 {@code FacAlarm} 实体转换而来，避免实体直接序列化导致字段失配。
 */
@Data
public class AlarmItem {

    private String alarmId;
    private Integer level;
    private String type;
    /** ACTIVE / ACKED / DISPATCHED / CLOSED */
    private String status;
    private String deviceCode;
    private String location;
    private LocalDateTime ts;
    private String description;
    private String category;
    private Boolean warned;
    private String title;
    private String planId;
}
