package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 工业电视视频监控点位档案（V24 真实表，替代大屏硬编码监控详情映射）。 */
@Data
@TableName("fac_tv_monitor")
public class FacTvMonitor {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String monitorCode;
    private String monitorName;
    private Boolean online;
    private String integrity;
    private String monitorType;
    private String department;

    /** 防区编码（关联 sys_zone.zone_code，V86 建立防区维度）。 */
    @TableField("zone_code")
    private String zoneCode;

    /**
     * 监控分类 code（V87 建立概览实时聚合维度）：PRODUCTION 生产设施 / BOUNDARY 厂界 /
     * CLOSED_GATE 封闭入口 / OTHER_GATE 其他入口 / OTHER 其它。与 fac_tv_stat_item.OVERVIEW
     * 分类 label 对齐（重大危险源例外，概览计数走 fac_major_hazard）。空表示未分类。
     */
    @TableField("monitor_category")
    private String monitorCategory;

    /** 安装位置坐标描述；列名 location_desc 避免与通用列冲突 */
    @TableField("location_desc")
    private String location;

    /** 挂高文案（如 24m）；列名 height_text */
    @TableField("height_text")
    private String height;

    /** 安装角度文案（如 56°）；列名 angle_text */
    @TableField("angle_text")
    private String angle;
}
