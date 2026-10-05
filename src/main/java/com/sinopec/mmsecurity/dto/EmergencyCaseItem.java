package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

import java.io.Serializable;

/**
 * 事故案例库条目，与前端 {@code emergency.openapi.json#/EmergencyCaseItem} 对齐。
 */
@Data
public class EmergencyCaseItem implements Serializable, ZoneAware {

    /** 案例 ID */
    private String id;

    /** 事故名称 */
    private String title;

    /** 事故类型 */
    private String accidentType;

    /** 事故地点 */
    private String location;

    /** 发生时间（yyyy-MM-dd HH:mm:ss） */
    private String occurredAt;

    /** 案例摘要 */
    private String summary;

    /** 经验教训 / 启示 */
    private String lessons;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（事故发生位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
