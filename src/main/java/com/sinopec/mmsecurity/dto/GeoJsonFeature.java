package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * GeoJSON Feature，与前端 {@code map.openapi.json#/GeoJsonFeatureCollection} 内 features 项对齐。
 * properties 为业务属性（alarm/device 字段，additionalProperties 容忍后端扩展）。
 */
@Data
public class GeoJsonFeature implements Serializable {

    /** 固定为 Feature */
    private String type = "Feature";
    /** 业务属性（alarm: alarmId/level/name/status/type；device: deviceCode/name/status） */
    private Map<String, Object> properties;
    /** GeoJSON 几何 */
    private GeoJsonGeometry geometry;
}
