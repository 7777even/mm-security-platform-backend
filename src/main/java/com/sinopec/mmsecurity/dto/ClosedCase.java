package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 已结案事件项，与前端 {@code emergency.openapi.json#/ClosedCase} 对齐。
 * 由 fac_alarm(status=3 CLOSED) 映射：caseId=alarmId / title / location / closedAt=occurred_at。
 */
@Data
public class ClosedCase implements Serializable {

    /** 结案 ID */
    private String caseId;
    /** 事件标题 */
    private String title;
    /** 事发位置 */
    private String location;
    /** 结案时间（ISO8601） */
    private LocalDateTime closedAt;
    /** 处置人（fac_alarm 无该字段，默认「系统归档」） */
    private String handler;
}
