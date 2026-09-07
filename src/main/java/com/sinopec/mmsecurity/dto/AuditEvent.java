package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 审计事件项，与前端 {@code uplink.openapi.json#/AuditEvent} 对齐。
 */
@Data
public class AuditEvent implements Serializable {

    /** 操作动作标识（必填） */
    @NotBlank(message = "action 必填")
    private String action;

    /** 所属模块（可选） */
    private String module;

    /** 扩展上下文（可选，additionalProperties） */
    private Map<String, Object> detail;

    /** 事件时间戳（毫秒，可选，缺省服务端取当前） */
    private Long at;
}
