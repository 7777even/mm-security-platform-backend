package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 通讯通知记录写请求（新建/更新共用）。
 *
 * <p>id 由服务端按 {@code LedgerIdSupport} 分配（规避 H2/PG/DM 自增序列滞后撞主键）；
 * recordNo 为业务自然键（与列表 recordNo 一致）。字段语义同构五类记录，差异收敛到
 * channel / direction / contentType 三个附加列；result 落到实体的 result_text 列。
 */
@Data
public class CommRecordWriteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String recordNo;

    @NotBlank
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
