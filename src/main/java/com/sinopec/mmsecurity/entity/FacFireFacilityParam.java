package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施监测 - 监控参数实体（对应 H2 表 fac_fire_facility_param）。
 * value_text 列 + valueText 属性：H2 中 value 为保留字，既不能作列名也不能作 MyBatis-Plus 别名。
 * tone：normal/warning/danger。
 */
@Data
@TableName(value = "fac_fire_facility_param")
public class FacFireFacilityParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long monitorId;

    private String label;

    @TableField("value_text")
    private String valueText;

    private String tone;

    private Integer sortNo;
}
