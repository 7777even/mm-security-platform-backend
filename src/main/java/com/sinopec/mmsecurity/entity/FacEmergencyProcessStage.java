package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急流程节点实体（对应 H2 表 fac_emergency_process_stage），数据源 V31。
 *
 * <p>嵌套结构（previousContext / currentActions / criteriaChecklist / subStages / escalationRule）
 * 序列化存于 detail_json，与 V27 fac_emergency_cmd.detail_json 同范式。</p>
 */
@Data
@TableName(value = "fac_emergency_process_stage")
public class FacEmergencyProcessStage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 节点号（1-15） */
    private Integer stageId;

    /** 所属阶段编码（phase_code） */
    private String phaseCode;

    /** 节点完整结构 JSON */
    private String detailJson;

    private Integer sortNo;
}
