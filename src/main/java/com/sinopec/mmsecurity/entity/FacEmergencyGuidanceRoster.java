package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急指引实时值班表实体（对应 H2 表 fac_emergency_guidance_roster，单行），数据源 V31。 */
@Data
@TableName(value = "fac_emergency_guidance_roster")
public class FacEmergencyGuidanceRoster implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 值班班组（乙班（白班）） */
    private String shiftGroup;

    private String supervisor;

    private String supervisorPhone;

    private String boardOperator;

    private String boardOperatorPhone;

    private String fieldOperator;

    private String fieldOperatorPhone;

    private Integer sortNo;
}
