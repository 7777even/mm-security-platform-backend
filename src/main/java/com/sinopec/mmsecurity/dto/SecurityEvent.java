package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 安防门禁事件 DTO —— 与前端 {@code security.openapi.json#/SecurityEvent} 字节级对齐。
 * 数据来自真实表 fac_security_event。
 */
@Data
public class SecurityEvent {
    private String eventId;
    private String person;
    private String channel;
    private String cardId;
    private String vehicle;
    private String direction;
    private Integer level;
    private String ts;
}
