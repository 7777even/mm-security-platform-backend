package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 前端操作审计落库表。审计不可变，不带逻辑删除与审计字段（created_at 仅记录落库时间）。
 */
@Data
@TableName("fac_audit_log")
public class FacAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作动作标识（如 route.view） */
    private String action;

    /** 所属模块（可选） */
    private String module;

    /** 扩展上下文 JSON 文本（detail 序列化存储） */
    @TableField("detail_json")
    private String detailJson;

    /** 事件时间戳（毫秒，可选，缺省落库时间） */
    private Long eventAt;

    private LocalDateTime createdAt;
}
