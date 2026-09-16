package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 通讯通知记录条目（五类记录的统一视图，各类型按需填充字段）。
 *
 * <p>字段名与前端契约 docs/api/communication.openapi.json 的 CommunicationRecord schema 完全一致。
 */
@Data
public class CommunicationRecord {
    private String recordNo;
    private String recordType;
    private String occurredAt;
    private String category;
    private String sender;
    private String receiver;
    private String summary;
    private String result;
    private String duration;
    private String channel;
    private String direction;
    private String contentType;
}
