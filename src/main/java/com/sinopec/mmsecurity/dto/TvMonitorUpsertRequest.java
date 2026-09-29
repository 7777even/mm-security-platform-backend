package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 视频监控点位（fac_tv_monitor）新增/更新请求（设备/防区管理 CRUD）。
 * 字段与实体一致，仅非空字段在更新时覆盖。
 */
@Data
public class TvMonitorUpsertRequest {
    /** 监控点位编码（新增必填，全局唯一） */
    private String monitorCode;
    /** 监控名称 */
    private String monitorName;
    /** 是否在线 */
    private Boolean online;
    /** 完好程度：良好 / 一般 / 损坏 */
    private String integrity;
    /** 监控类型：球机 / 枪机 */
    private String monitorType;
    /** 责任部门 */
    private String department;
    /** 防区编码（关联 sys_zone.zone_code，防区归属编辑） */
    private String zoneCode;
    /** 安装位置坐标描述 */
    private String location;
    /** 挂高文案（如 24m） */
    private String height;
    /** 安装角度文案（如 56°） */
    private String angle;
}
