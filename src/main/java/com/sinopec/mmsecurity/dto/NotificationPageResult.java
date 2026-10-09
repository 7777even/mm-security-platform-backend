package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 通知分页结果（与前端 PageResult 同构：list/total/page/size/unreadCount）。 */
@Data
public class NotificationPageResult implements Serializable {
    private List<NotificationItem> list;
    private long total;
    private long page;
    private long size;
    /** 当前收件箱未读数（用于铃铛徽标） */
    private long unreadCount;
}
