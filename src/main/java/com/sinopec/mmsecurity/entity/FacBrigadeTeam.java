package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 消防队伍主表（对应 H2 表 fac_brigade_team）。
 * 队伍车辆 / 人员 / 装备分别落在 fac_brigade_vehicle、fac_brigade_person、fac_brigade_equipment 子表。
 */
@Data
@TableName(value = "fac_brigade_team")
public class FacBrigadeTeam implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String teamName;

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

    private Integer sortNo;
}
