package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

import java.util.List;

/** 应急救援资源 - 消防队伍，字段与前端 FireBrigadeTeam 一致（含车辆/人员/装备子集合）。 */
@Data
public class FireBrigadeTeam implements ZoneAware {
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

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（消防队伍驻地），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
