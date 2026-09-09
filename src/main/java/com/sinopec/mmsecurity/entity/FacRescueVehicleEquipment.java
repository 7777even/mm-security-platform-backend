package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急救援资源 - 救援车辆随车装备（对应 H2 表 fac_rescue_vehicle_equipment），按 vehicle_id 关联主表。 */
@Data
@TableName(value = "fac_rescue_vehicle_equipment")
public class FacRescueVehicleEquipment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;

    private String equipName;

    /** 数量以字符串形式存储（如“4 套”），与前端展示口径一致。 */
    private String quantity;

    private String equipModel;

    private String nextCheckDate;

    private String equipmentStatus;

    private String storageLocation;

    private Integer sortNo;
}
