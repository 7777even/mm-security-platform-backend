package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 应急流程全景聚合：阶段 + 响应模式 + 节点，与前端 emergencyProcessData 对齐（契约 #/EmergencyProcessPanorama）。 */
@Data
public class EmergencyProcessPanorama implements Serializable {

    /** 应急阶段（5 个，按 start 升序） */
    private List<EmergencyPhase> phases;

    /** 响应模式选项（4 个，登台/升级下拉用） */
    private List<ResponseModeOption> responseModes;

    /** 流程节点（15 个，按节点号升序） */
    private List<ProcessStage> stages;
}
