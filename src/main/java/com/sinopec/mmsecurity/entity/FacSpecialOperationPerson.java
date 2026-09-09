package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 特殊作业 - 作业票作业人员实体（对应 H2 表 fac_special_operation_person）。 */
@Data
@TableName(value = "fac_special_operation_person")
public class FacSpecialOperationPerson implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ticketId;

    private String name;

    @TableField("role_name")
    private String roleName;

    private String phone;
}
