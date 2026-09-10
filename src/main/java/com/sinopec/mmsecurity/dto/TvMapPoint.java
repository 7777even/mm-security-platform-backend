package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 工业电视地图视频点位，与前端
 * {@code tv.openapi.json#/components/schemas/TvMapPoint} 对齐。
 */
@Data
public class TvMapPoint implements Serializable {

    /** 点位编码 */
    private String id;
    /** 点位名称 */
    private String label;
    /** 分组：high-ar / focus / hazard / boundary */
    private String group;
    /** 经度 */
    private Double longitude;
    /** 纬度 */
    private Double latitude;
    /** 挂高（米） */
    private Integer height;
    /** 是否在线 */
    private Boolean online;
}
