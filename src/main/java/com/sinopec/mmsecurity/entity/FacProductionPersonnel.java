package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产应急 - ProductionPersonnel 实体（人员定位标记，对应 H2 表 fac_production_personnel）。
 * left_ratio/top_ratio 为舞台百分比字符串（H2 中 LEFT / TOP 为保留字，故加 _ratio 后缀）；
 * longitude/latitude 为按 map.pgw 换算的 WGS84 真实坐标，供地图打点。
 */
@Data
@TableName(value = "fac_production_personnel")
public class FacProductionPersonnel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String leftRatio;

    private String topRatio;

    private Double longitude;

    private Double latitude;

    private String location;

    private Integer personCount;

    private String markerIcon;

    private String popupBg;

    private String markerDot;

    private String markerLine;

    private Integer sortNo;
}
