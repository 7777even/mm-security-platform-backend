package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * GeoJSON 几何（Point/LineString/Polygon/Multi*），与前端 {@code map.openapi.json} 对齐。
 * Point 坐标为 [经度, 纬度]（WGS84，单位度）。
 */
@Data
public class GeoJsonGeometry implements Serializable {

    /** 几何类型（Point 等） */
    private String type;
    /** 坐标数组；Point=[lng, lat] */
    private List<Double> coordinates;
}
