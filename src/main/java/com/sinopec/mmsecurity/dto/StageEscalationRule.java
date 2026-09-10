package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 节点升级规则，与前端 {@code ProcessStage.escalationRule} 对齐（契约 #/StageEscalationRule）。 */
@Data
public class StageEscalationRule implements Serializable {

    /** 触发升级的条件 */
    private String triggerCondition;

    /** 升级请求方角色 */
    private String fromRole;

    /** 升级接收方角色 */
    private String toRole;

    /** 现场概况 */
    private StageEscalationDetails details;
}
