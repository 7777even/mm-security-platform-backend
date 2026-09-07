package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * GeoJSON FeatureCollection（WGS84 经纬度，单位度），与前端 {@code map.openapi.json#/GeoJsonFeatureCollection} 对齐。
 */
@Data
public class GeoJsonFeatureCollection implements Serializable {

    /** 固定为 FeatureCollection */
    private String type = "FeatureCollection";
    /** 要素数组 */
    private List<GeoJsonFeature> features;
}
