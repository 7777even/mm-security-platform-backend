package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 应急救援资源 - 救援车辆条目（列表与详情同一批对象，含乘员/随车装备/耗材/出动汇总）。 */
@Data
public class RescueVehicleItem {
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
}
