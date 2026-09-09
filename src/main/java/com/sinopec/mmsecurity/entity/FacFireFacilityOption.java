package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施监测 - 下拉选项实体（对应 H2 表 fac_fire_facility_option）。
 * kind：FACILITY_TYPE=设施类型（全部类型 + 12 类标准类型 + 维护保养记录）。
 */
@Data
@TableName(value = "fac_fire_facility_option")
public class FacFireFacilityOption implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String kind;

    private String optionLabel;

    private Integer sortNo;
}
