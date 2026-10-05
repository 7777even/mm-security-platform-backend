package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 设施台账条目（含维保记录子表）。 */
@Data
public class FireFacilityLedgerItem implements Serializable, ZoneAware {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String facilityCode;
    private String facilityName;
    private String facilityType;
    private String location;
    private String device;
    private String maintainerName;
    private String maintainerPhone;
    private Boolean enabled;
    private List<FireFacilityMaintenanceRecord> maintenanceRecords;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（消防设施安装位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
