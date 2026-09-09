package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 台风易涝风险点位。 */
@Data
@TableName("fac_typhoon_map_risk_point")
public class FacTyphoonMapRiskPoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属事件 id */
    private Long incidentId;
    /** 点位编码 */
    private String pointCode;
    /** 点位名称 */
    private String pointName;
    /** 经度 */
    private Double longitude;
    /** 纬度 */
    private Double latitude;
    /** 状态 */
    private String statusName;
    /** 状态描述 */
    private String statusText;
    /** 责任单位 */
    private String responsibleUnit;
    /** 是否已前置部署 */
    private Boolean predeployed;
    /** 部署方案 */
    private String deployment;
    /** 标签 X 偏移 */
    private Integer labelOffsetX;
    /** 标签 Y 偏移 */
    private Integer labelOffsetY;
    /** 聚合数量 */
    private Integer clusterCount;
    /** 点位类型 */
    private String kind;
    /** 关联视频点位 id，逗号分隔 */
    private String videoIds;
    /** 排序号 */
    private Integer sortNo;}
