package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产应急 - ProductionDevice 实体（设备清单，对应 H2 表 fac_production_device）。
 * category 与设备分类总览卡片名称一致，status_name 取 正常 / 离线 / 故障。
 */
@Data
@TableName(value = "fac_production_device")
public class FacProductionDevice implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String category;

    private String name;

    private String typeName;

    private String area;

    private String statusName;

    private Double longitude;

    private Double latitude;

    private Integer sortNo;
}
