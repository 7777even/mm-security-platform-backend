package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

/**
 * 道闸（门禁卡口）列表项 DTO —— 与前端 {@code security.openapi.json#/GateControlItem} 字节级对齐。
 * 数据来自真实表 fac_gate_control。
 */
@Data
public class GateControlItem implements ZoneAware {
    private Long id;
    private String name;
    private String location;
    private String status;
    private Double longitude;
    private Double latitude;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（道闸安装位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
