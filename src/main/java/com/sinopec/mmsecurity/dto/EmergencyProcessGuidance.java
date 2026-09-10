package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 应急流程节点指引聚合：实时值班表 + 各节点处置指引（契约 #/EmergencyProcessGuidance）。 */
@Data
public class EmergencyProcessGuidance implements Serializable {

    /** 实时值班表（弹窗页眉用） */
    private GuidanceDutyRoster dutyRoster;

    /** 节点处置指引（按节点号升序） */
    private List<NodeGuidance> guidances;
}
