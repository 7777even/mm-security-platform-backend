package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

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
public class AlarmItem implements ZoneAware {

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

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（报警位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
