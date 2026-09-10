package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应急流程节点联动配置，与前端 {@code nodeConfigData.ts#NodePhaseConfig} 对齐
 * （契约 {@code emergency.openapi.json#/NodePhaseConfig}）。
 *
 * <p>控制节点切换时的地图镜头锚点、左右面板显隐与值班自动排班，数据源 V30 fac_node_phase_config。</p>
 */
@Data
public class NodePhaseConfig implements Serializable {

    /** 节点 id（alarmJudgement / 1min / 3min / 5min / plantArea / companyLevel / govLevel / handling / archive） */
    private String nodeId;

    /** 节点名称 */
    private String nodeName;

    /** 3D 地图镜头配置 */
    private NodePhaseMapCamera mapCamera;

    /** 右侧面板隐藏页签（duty/auxiliary/dynamics 的子集） */
    private List<String> rightPanelHiddenTabs;

    /** 左侧面板隐藏面板（incident/plan/info 的子集） */
    private List<String> leftPanelHiddenPanels;

    /** 值班配置 */
    private NodePhaseDuty duty;
}
