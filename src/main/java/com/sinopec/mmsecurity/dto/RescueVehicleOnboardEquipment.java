package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急救援资源 - 车辆随车装备，字段与前端 RescueVehicleOnboardEquipment 一致（quantity 为字符串）。 */
@Data
public class RescueVehicleOnboardEquipment {
    private String name;
    private String quantity;
    private String model;
    private String nextCheckDate;
    private String equipmentStatus;
    private String storageLocation;
}
