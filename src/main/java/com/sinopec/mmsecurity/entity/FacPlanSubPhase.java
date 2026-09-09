package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急预案 - 子阶段实体（对应 H2 表 fac_plan_sub_phase），parent_code 指向大阶段 phase_code。 */
@Data
@TableName(value = "fac_plan_sub_phase")
public class FacPlanSubPhase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instanceId;

    private String phaseCode;

    private String parentCode;

    private String phaseName;

    private Integer phaseOrder;

    private Integer progress;
}
