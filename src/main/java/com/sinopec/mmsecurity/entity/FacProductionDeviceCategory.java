package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产应急 - ProductionDeviceCategory 实体（设备分类总览卡片，对应 H2 表 fac_production_device_category）。
 * name 与 fac_production_device.category 语义一致，用于设备清单按分类过滤。
 */
@Data
@TableName(value = "fac_production_device_category")
public class FacProductionDeviceCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer itemCount;

    private String image;

    private Integer sortNo;
}
