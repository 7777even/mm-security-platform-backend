package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 工业电视视频监控点位档案，与前端
 * {@code tv.openapi.json#/components/schemas/TvMonitorDetail} 对齐。
 */
@Data
public class TvMonitorDetail implements Serializable {

    /** 点位编码 */
    private String id;
    /** 监控名称 */
    private String name;
    /** 是否在线 */
    private Boolean online;
    /** 完好程度（良好 / 一般 / 损坏） */
    private String integrity;
    /** 监控类型（球机 / 枪机） */
    private String monitorType;
    /** 责任部门 */
    private String department;
    /** 防区编码（关联 sys_zone.zone_code，V86 建立防区维度） */
    private String zoneCode;
    /** 防区名称（由 zone_code 关联 sys_zone.zone_name 解析，便于前端直接展示） */
    private String zoneName;
    /** 安装位置坐标描述 */
    private String location;
    /** 挂高文案 */
    private String height;
    /** 安装角度文案 */
    private String angle;
}
