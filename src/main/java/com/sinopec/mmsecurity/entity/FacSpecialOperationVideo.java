package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 特殊作业 - 作业票现场视频实体（对应 H2 表 fac_special_operation_video）。 */
@Data
@TableName(value = "fac_special_operation_video")
public class FacSpecialOperationVideo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ticketId;

    private String name;

    private String location;
}
