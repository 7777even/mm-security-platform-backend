package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急救援资源 - 车辆乘员，字段与前端 rescueVehicleMock.ts 的 RescueVehicleCrewMember 一致。 */
@Data
public class RescueVehicleCrewMember {
    private String role;
    private String name;
    private String phone;
    private String certificate;
    private String dutyStatus;
}
