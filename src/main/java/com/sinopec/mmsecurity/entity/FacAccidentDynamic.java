package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 事故救援 - AccidentDynamic 实体（对应 H2 表 fac_accidentdynamic）。
 */
@Data
@TableName(value = "fac_accident_dynamic")
public class FacAccidentDynamic implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String category;

    private String title;

    private String tag;

    private String time;

    @TableField("command_text")
    private String commandText;

    private String responder;

    private String reply;

    private String stageLabel;

    private Integer sortNo;
}
