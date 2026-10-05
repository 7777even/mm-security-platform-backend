package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

/**
 * 通讯设备 - 单台设备（id 为设备编码字符串，如 'bc-a1'）。
 * 字段名与前端 mock 的 CommunicationDevice 完全一致。
 */
@Data
public class CommunicationDevice implements ZoneAware {
    private String id;
    private String type;
    private String name;
    private String area;
    private String location;
    private String status;
    private Double longitude;
    private Double latitude;
    private CommunicationDeviceDetail detail;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（通讯设备安装位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
