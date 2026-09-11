package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急辅助信息统计实体（对应 H2 表 fac_emergency_assist_stat，V39）。
 *
 * <p>取代前端 EmergencyAssistPanel 硬编码的 4 项 KPI（应急预案/现场处置卡/应急联络人/可用消防水源）。</p>
 */
@Data
@TableName(value = "fac_emergency_assist_stat")
public class FacEmergencyAssistStat implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 统计项标签：应急预案 / 现场处置卡 / 应急联络人 / 可用消防水源 */
    private String label;

    /** 数值（如应急预案 15 套的 15）；列名 stat_value 以避开 H2 保留字 value */
    @TableField("stat_value")
    private Integer value;

    /** 单位：套 / 张 / 人 / 处 */
    private String unit;

    /** 配色：blue / cyan / green / orange */
    private String tone;

    private Integer sortNo;
}
