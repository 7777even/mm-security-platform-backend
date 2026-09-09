package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 事故救援 - AccidentDutyPerson 实体（对应 H2 表 fac_accidentdutyperson）。
 */
@Data
@TableName(value = "fac_accident_duty_person")
public class FacAccidentDutyPerson implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String role;

    private String phone;

    private Integer avatarIndex;

    private Integer sortNo;
}
