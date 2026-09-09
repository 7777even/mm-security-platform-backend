package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 人员定位标记（对应前端 PersonnelMarker）。
 * left / top 为舞台百分比坐标（版面定位用），longitude / latitude 为 WGS84 真实坐标（地图打点用）。
 */
@Data
public class PersonnelMarker implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String left;
    private String top;
    private Double longitude;
    private Double latitude;
    private String location;
    private Integer count;
    private String markerIcon;
    private String popupBg;
    private String markerDot;
    private String markerLine;
}
