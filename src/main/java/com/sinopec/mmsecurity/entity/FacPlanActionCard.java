package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急预案 - 行动卡片实体（对应 H2 表 fac_plan_action_card）。
 * card_status：pending=待执行 / in-progress=执行中 / completed=已完成；is_global 为跨阶段全局卡。
 */
@Data
@TableName(value = "fac_plan_action_card")
public class FacPlanActionCard implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instanceId;

    private String cardCode;

    private String resourceCode;

    private String title;

    private String contentText;

    private String descriptionText;

    private String startSubPhaseCode;

    private String endSubPhaseCode;

    private String riskEventCode;

    private String cardStatus;

    private Boolean isGlobal;

    private Integer sortNo;
}
