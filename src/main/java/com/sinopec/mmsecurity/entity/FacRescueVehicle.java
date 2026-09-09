package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 救援车辆主表（对应 H2 表 fac_rescue_vehicle）。
 * 乘员 / 随车装备 / 耗材 / 出动汇总分别落在 fac_rescue_vehicle_crew、_equipment、_kv 子表。
 */
@Data
@TableName(value = "fac_rescue_vehicle")
public class FacRescueVehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String plate;

    private String vehicleType;

    private String squadron;

    private String leaderName;

    private String leaderPhone;

    private String vehicleStatus;

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

    private Integer sortNo;
}
