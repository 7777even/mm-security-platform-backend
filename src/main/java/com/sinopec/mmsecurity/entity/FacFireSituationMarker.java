package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 火情态势地图聚合点位实体（对应 H2 表 fac_fire_situation_marker）。
 * marker_code：前端使用的字符串点位标识（如 event-1 / op-hot / alarm-1）。
 * marker_kind：event=应急事件 / operation=作业票 / alarm=报警。
 */
@Data
@TableName(value = "fac_fire_situation_marker")
public class FacFireSituationMarker implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String markerCode;

    private String markerKind;

    private String title;

    private String subtitle;

    private Double longitude;

    private Double latitude;

    private Boolean importantFlag;

    private String iconUrl;

    private String levelName;

    private Long targetId;

    private Integer sortNo;
}
