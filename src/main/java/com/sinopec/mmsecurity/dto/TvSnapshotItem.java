package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 录像截图列表项。 */
@Data
public class TvSnapshotItem {

    private Long id;
    private String monitorCode;
    private String monitorName;
    private String captureTime;
    private String eventType;
    /** PENDING 待确认 / ACKED 已确认。 */
    private String reviewStatus;
    /** DEVICE 设备采集 / MANUAL 手工。 */
    private String source;
    private String createdAt;
    /** 是否含截图字节（供前端决定是否请求 blob 端点）。 */
    private Boolean hasImage;
    /** 防区编码（关联 sys_zone.zone_code，V86 建立防区维度）。 */
    private String zoneCode;
    /** 防区名称（由 zone_code 解析，便于前端直接展示）。 */
    private String zoneName;

    /**
     * 关联告警 id（跨域联动）。空表示未关联。
     */
    private Long alarmId;

    /**
     * 关联告警类型：PRODUCTION 生产 / FIRE 消防 / PERIMETER 周界；空表示未关联。
     */
    private String alarmType;
}
