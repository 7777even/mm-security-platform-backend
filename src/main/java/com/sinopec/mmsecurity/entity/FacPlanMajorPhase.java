package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急预案 - 大阶段实体（对应 H2 表 fac_plan_major_phase），upgrade_process 为上报与升级流程。 */
@Data
@TableName(value = "fac_plan_major_phase")
public class FacPlanMajorPhase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instanceId;

    private String phaseCode;

    private String phaseName;

    private Integer phaseOrder;

    private String upgradeProcess;
}
