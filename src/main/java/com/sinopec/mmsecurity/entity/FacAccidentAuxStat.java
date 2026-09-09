package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 事故救援 - AccidentAuxStat 实体（对应 H2 表 fac_accidentauxstat）。
 */
@Data
@TableName(value = "fac_accident_aux_stat")
public class FacAccidentAuxStat implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String label;

    @TableField("value_name")
    private Integer valueName;

    private Integer iconIndex;

    private Integer sortNo;
}
