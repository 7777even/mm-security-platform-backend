package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 值班配置，与前端 {@code nodeConfigData.ts#NodePhaseConfig.duty} 对齐。 */
@Data
public class NodePhaseDuty {

    /** 是否按节点切换值班小组自动排班 */
    private Boolean autoRoster;
}
