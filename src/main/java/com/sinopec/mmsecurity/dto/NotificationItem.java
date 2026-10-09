package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 通知项（sys_notification 只读投影，供消息中心/底部播报消费）。 */
@Data
public class NotificationItem implements Serializable {
    private Long id;
    /** 消息分类：alarm / event / task / system */
    private String category;
    private String title;
    private String summary;
    /** 是否已读 */
    private Boolean read;
    /** 落库时间（yyyy-MM-dd HH:mm:ss） */
    private String createdAt;
    /** 关联业务对象（可选） */
    private NotificationTarget target;
}
