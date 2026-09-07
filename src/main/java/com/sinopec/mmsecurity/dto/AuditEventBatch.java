package com.sinopec.mmsecurity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 审计事件批次，与前端 {@code uplink.openapi.json#/AuditEventBatch} 对齐。
 * 批量上报前端操作审计（登录/路由查看/指令查看等），异步尽力落库。
 */
@Data
public class AuditEventBatch implements Serializable {

    /** 审计事件批次（非空） */
    @NotEmpty(message = "events 必填")
    @Valid
    private List<AuditEvent> events;
}
