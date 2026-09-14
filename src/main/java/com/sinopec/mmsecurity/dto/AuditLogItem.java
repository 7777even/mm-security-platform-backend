package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 审计日志项（fac_audit_log 只读投影，供后台管理端审计日志页展示）。 */
@Data
public class AuditLogItem implements Serializable {

    /** 自增主键 */
    private Long id;

    /** 操作动作标识（如 login / system.user.create） */
    private String action;

    /** 所属模块（可选） */
    private String module;

    /** 扩展上下文 JSON 文本（detail 序列化，可能为 null） */
    private String detailJson;

    /** 事件时间戳（毫秒，可能为 null） */
    private Long eventAt;

    /** 落库时间（yyyy-MM-dd HH:mm:ss） */
    private String createdAt;
}
