package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急预案 - 预案切换目录实体（对应 H2 表 fac_emergency_plan）。
 * tab_key：disposal=应急处置方案 / fire=消防救援预案 / company=公司级应急预案 / superior=上级单位应急预案。
 */
@Data
@TableName(value = "fac_emergency_plan")
public class FacEmergencyPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tabKey;

    private String planName;

    private String accidentType;

    private String facility;

    private Integer sortNo;
}
