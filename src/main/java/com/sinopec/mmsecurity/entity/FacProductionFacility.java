package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产应急 - ProductionFacility 实体（设施总览卡片，对应 H2 表 fac_production_facility）。
 * 供大屏首屏「设施总览」网格使用，同时作为装置区二级页的设施维度主键。
 */
@Data
@TableName(value = "fac_production_facility")
public class FacProductionFacility implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer itemCount;

    private String image;

    private Integer sortNo;
}
