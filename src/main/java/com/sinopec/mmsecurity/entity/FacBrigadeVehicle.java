package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急救援资源 - 消防队伍车辆（对应 H2 表 fac_brigade_vehicle），按 team_id 关联队伍。 */
@Data
@TableName(value = "fac_brigade_vehicle")
public class FacBrigadeVehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private String plate;

    private String vehicleType;

    private String vehicleStatus;

    private String parkingLocation;

    private Integer sortNo;
}
