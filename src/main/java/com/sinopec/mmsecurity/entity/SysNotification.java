package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知（消息中心收件箱）。
 *
 * <p>recipient 为 null 表示全员广播；read_flag 标记已读（0 未读 / 1 已读）。
 * 收件范围 = 本人专属 + 全员广播，由 {@code NotificationService} 按当前登录态过滤。</p>
 */
@Data
@TableName("sys_notification")
public class SysNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息分类：alarm / event / task / system */
    @TableField("category")
    private String category;

    /** 标题 */
    @TableField("title")
    private String title;

    /** 摘要 */
    @TableField("summary")
    private String summary;

    /** 业务对象类型（alarm/event/task），可选 */
    @TableField("target_type")
    private String targetType;

    /** 业务对象 ID，可选 */
    @TableField("target_id")
    private String targetId;

    /** 接收人（null = 全员广播） */
    @TableField("recipient")
    private String recipient;

    /** 已读标记：0 未读 / 1 已读 */
    @TableField("read_flag")
    private Integer readFlag;

    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
