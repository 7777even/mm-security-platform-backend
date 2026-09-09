package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急救援资源 - 救援车辆乘员（对应 H2 表 fac_rescue_vehicle_crew），按 vehicle_id 关联主表。 */
@Data
@TableName(value = "fac_rescue_vehicle_crew")
public class FacRescueVehicleCrew implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;

    private String memberRole;

    private String memberName;

    private String phone;

    private String certificate;

    private String dutyStatus;

    private Integer sortNo;
}
