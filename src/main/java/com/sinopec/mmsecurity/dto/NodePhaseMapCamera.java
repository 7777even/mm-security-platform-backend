package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 3D 地图镜头配置，与前端 {@code nodeConfigData.ts#MapCameraConfig} 对齐
 * （契约 {@code emergency.openapi.json#/NodePhaseMapCamera}）。
 */
@Data
public class NodePhaseMapCamera {

    /** 镜头中心锚点优先级（按序降级：事件装置 → 报警电话位置 → 防区 → 抢险队 GPS → 全厂 → 自定义） */
    private List<String> anchorPriorityList;

    /** 自定义镜头中心 [lon, lat]；未配置时为 null */
    private List<Double> customCenter;

    /** 缓冲区半径（米） */
    private Integer bufferRadiusMeters;
}
