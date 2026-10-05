package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

import java.util.List;

/** 应急救援资源 - 救援车辆条目（列表与详情同一批对象，含乘员/随车装备/耗材/出动汇总）。 */
@Data
public class RescueVehicleItem implements ZoneAware {
    private Long id;
    private String plate;
    private String type;
    private String squadron;
    private String leaderName;
    private String leaderPhone;
    private String status;
    /** 业务对象名称，用于详情标题 */
    private String businessName;
    private String vehicleTypeFull;
    private String parkingLocation;
    private String chassisModel;
    private String manufactureDate;
    private String inspectionExpiry;
    private String foamTankVolume;
    private String waterTankVolume;
    private String maxWaterFlow;
    private String foamType;
    private String lastMaintenanceDate;
    private String nextMaintenanceDate;
    private String totalMileage;
    private String faultRecord;
    private String inspectionStatus;
    private List<RescueVehicleCrewMember> crew;
    private List<RescueVehicleOnboardEquipment> onboardEquipment;
    private List<KvItem> consumables;
    private List<KvItem> dispatchSummary;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 parkingLocation（救援车辆停放位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.parkingLocation;
    }
}
