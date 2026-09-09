package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 易涝风险点位，与 typhoon-emergency.openapi.json#/TyphoonMapRiskPoint 对齐。
 */
@Data
public class TyphoonMapRiskPoint implements Serializable {

    /** 点位编码 */
    private String id;
    /** 点位名称 */
    private String name;
    /** 经度 WGS84 */
    private Double longitude;
    /** 纬度 WGS84 */
    private Double latitude;
    /** 状态 */
    private String status;
    /** 状态描述 */
    private String statusText;
    /** 责任单位 */
    private String responsibleUnit;
    /** 是否已前置部署 */
    private Boolean predeployed;
    /** 部署方案 */
    private String deployment;
    /** 标签 X 像素偏移 */
    private Integer labelOffsetX;
    /** 标签 Y 像素偏移 */
    private Integer labelOffsetY;
    /** 聚合点位数量 */
    private Integer clusterCount;
    /** 点位类型 risk / resource */
    private String kind;
    /** 关联视频点位 id 列表（默认空列表，避免前端空指针） */
    private List<String> videoIds = new ArrayList<>();
}
