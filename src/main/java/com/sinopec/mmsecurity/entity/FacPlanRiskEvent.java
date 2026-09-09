package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 应急预案 - 风险事件实体（对应 H2 表 fac_plan_risk_event），sub_phase_code 指向子阶段。 */
@Data
@TableName(value = "fac_plan_risk_event")
public class FacPlanRiskEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instanceId;

    private String eventCode;

    private String subPhaseCode;

    private String eventName;
}
