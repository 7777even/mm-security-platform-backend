package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 工业电视地图视频点位（V24 真实表，替代大屏硬编码 tvVideoMapPoints）。 */
@Data
@TableName("fac_tv_map_point")
public class FacTvMapPoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pointCode;
    private String pointLabel;
    private String pointGroup;
    private Double longitude;
    private Double latitude;

    /** 挂高（米）；列名 point_height 避免与保留字/通用列冲突 */
    @TableField("point_height")
    private Integer height;

    private Boolean online;
    private Integer sortNo;
}
