package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 应急救援资源 - 消防队伍，字段与前端 FireBrigadeTeam 一致（含车辆/人员/装备子集合）。 */
@Data
public class FireBrigadeTeam {
    private Long id;
    private String name;
    private String area;
    private Integer memberCount;
    private String leaderName;
    private String leaderPhone;
    private String location;
    private Double longitude;
    private Double latitude;
    private String description;
    private Integer rescuePersonnel;
    private Integer rescueVehicles;
    private List<FireBrigadeVehicle> vehicles;
    private List<FireBrigadePerson> personnel;
    private List<FireBrigadeEquipment> equipment;
}
