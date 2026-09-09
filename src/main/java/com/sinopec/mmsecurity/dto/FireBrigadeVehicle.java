package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急救援资源 - 消防队伍车辆，字段与前端 fireBrigadeMock.ts 的 FireBrigadeVehicle 一致。 */
@Data
public class FireBrigadeVehicle {
    private Long id;
    private String plate;
    private String type;
    private String status;
    private String parkingLocation;
}
