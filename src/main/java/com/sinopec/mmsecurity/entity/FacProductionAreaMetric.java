package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产应急 - ProductionAreaMetric 实体（装置区指标卡，对应 H2 表 fac_production_area_metric）。
 * value_text 列 + valueText 属性：H2 中 value 为保留字，既不能作列名也不能作 MyBatis-Plus 别名。
 */
@Data
@TableName(value = "fac_production_area_metric")
public class FacProductionAreaMetric implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long facilityId;

    private String label;

    @TableField("value_text")
    private String valueText;

    private Integer sortNo;
}
