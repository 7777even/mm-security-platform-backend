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
}
