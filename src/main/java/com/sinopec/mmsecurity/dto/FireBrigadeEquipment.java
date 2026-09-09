package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急救援资源 - 消防队伍装备，字段与前端 FireBrigadeEquipment 一致。 */
@Data
public class FireBrigadeEquipment {
    private Long id;
    private String name;
    private String category;
    private Integer count;
    private String unit;
    private String status;
    private String storageLocation;
}
