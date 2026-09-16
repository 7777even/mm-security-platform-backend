package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 通讯通知记录列表（items + total）。
 *
 * <p>字段名与前端契约 docs/api/communication.openapi.json 的 CommunicationRecordList schema 完全一致。
 */
@Data
public class CommunicationRecordList {
    private List<CommunicationRecord> items;
    private Integer total;
}
