package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急预案目录实体（对应 H2 表 fac_emergency_plan_catalog，V39）。
 *
 * <p>取代前端 EmergencyPlanPanel 硬编码的 4 行预案层级（上级单位/公司级/消防救援/现场处置）。</p>
 */
@Data
@TableName(value = "fac_emergency_plan_catalog")
public class FacEmergencyPlanCatalog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预案层级编码：superior / company / branch / site（前端 key） */
    private String planCode;

    /** 层级标签：上级单位预案 / 公司级预案 / 消防救援预案 / 现场处置方案 */
    private String label;

    /** 当前生效的预案名称（未启动时为「未启动」） */
    private String planName;

    /** 是否可切换（0/1） */
    private Integer canSwitch;

    /** 是否为当前激活预案（0/1） */
    private Integer isCurrent;

    private Integer sortNo;
}
