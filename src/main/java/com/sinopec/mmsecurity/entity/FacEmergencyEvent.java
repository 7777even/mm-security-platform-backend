package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急事件实体（对应 H2 表 fac_emergency_event）。
 *
 * <p>scene：FIRE=消防应急 / PRELIMINARY=先期处置；kind：EVENT=应急事件 / DRILL=应急演练。
 * left_percent / top_percent 保留设计稿舞台百分比字符串（如 '47.1%'），
 * longitude / latitude 由舞台百分比经 map.pgw 换算得到的 WGS84 坐标（V15 同一先例）。
 */
@Data
@TableName(value = "fac_emergency_event")
public class FacEmergencyEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String scene;

    private String groupCode;

    private String groupLabel;

    private String kind;

    private String title;

    private String location;

    private String description;

    private String eventTime;

    private Boolean reported;

    private String status;

    private String statusLabel;

    private String leftPercent;

    private String topPercent;

    private Double longitude;

    private Double latitude;

    private String areaCode;

    private String eventCategory;

    private String hazardSourceLevel;

    private String endedAt;

    private String weatherType;

    private String warningLevel;

    private String affectedArea;

    private String monitoringPeriod;

    private String weatherSource;

    private String measures;

    private Integer sortNo;
}
