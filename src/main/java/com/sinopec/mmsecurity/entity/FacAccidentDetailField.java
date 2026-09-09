package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 事故救援 - AccidentDetailField 实体（对应 H2 表 fac_accidentdetailfield）。
 */
@Data
@TableName(value = "fac_accident_detail_field")
public class FacAccidentDetailField implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long incidentId;

    private String fieldLabel;

    private String fieldValue;

    private Integer sortNo;
}
