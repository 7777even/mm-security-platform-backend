package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急响应模式实体（对应 H2 表 fac_emergency_response_mode），数据源 V31。 */
@Data
@TableName(value = "fac_emergency_response_mode")
public class FacEmergencyResponseMode implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模式编码（team/plant/company/government） */
    private String modeCode;

    /** 模式名称（一、班组处置 …） */
    private String modeLabel;

    /** 该模式对应起始节点号 */
    private Integer stageId;

    private Integer sortNo;
}
