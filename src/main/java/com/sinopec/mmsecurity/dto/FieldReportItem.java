package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 现场采集回传项（防爆手机离线队列上行），与前端 {@code uplink.openapi.json#/FieldReportItem} 对齐。
 */
@Data
public class FieldReportItem implements Serializable {

    /** 回传项 ID（UUID，必填） */
    @NotBlank(message = "id 必填")
    private String id;

    /** 类型：field-report / task-ack */
    @NotBlank(message = "kind 必填")
    private String kind;

    /** 标题 */
    @NotBlank(message = "title 必填")
    private String title;

    /** 备注（可选） */
    private String note;

    /** 关联 20 位 MDM 设备编码（可选） */
    private String deviceCode;

    /** 附件媒体（可选） */
    private List<FieldReportMedia> media;

    /** 创建时间戳（毫秒） */
    private Long createdAt;

    /** 回传状态 */
    @NotBlank(message = "status 必填")
    private String status;

    /**
     * 回传提交人（服务端信任值，由 UplinkService 按当前登录态覆盖，
     * 客户端传入无效——防水平越权/身份冒用）。
     */
    private String reporter;

    /** 已尝试次数 */
    private int attempts;

    /** 最近错误（可选） */
    private String lastError;

    /** 同步完成时间戳（毫秒，可选） */
    private Long syncedAt;
}
