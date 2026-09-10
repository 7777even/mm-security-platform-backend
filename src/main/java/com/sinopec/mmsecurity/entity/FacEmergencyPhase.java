package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急流程阶段实体（对应 H2 表 fac_emergency_phase），数据源 V31。 */
@Data
@TableName(value = "fac_emergency_phase")
public class FacEmergencyPhase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phaseCode;

    private String phaseName;

    /** 起始节点号（含） */
    private Integer startStage;

    /** 结束节点号（含） */
    private Integer endStage;

    /** 阶段配色（blue/cyan/amber/red/green） */
    private String tone;

    private Integer sortNo;
}
