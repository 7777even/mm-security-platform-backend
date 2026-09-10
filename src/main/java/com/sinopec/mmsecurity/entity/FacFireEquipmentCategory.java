package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 消防设备分类（V24 真实表，替代大屏硬编码 fireEquipment）。 */
@Data
@TableName("fac_fire_equipment_category")
public class FacFireEquipmentCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String categoryName;
    private Integer equipCount;
    private Integer sortNo;
}
