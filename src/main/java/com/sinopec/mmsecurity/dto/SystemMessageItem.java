package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 大屏滚动系统消息项，与前端
 * {@code dashboard.openapi.json#/components/schemas/SystemMessageItem} 对齐。
 */
@Data
public class SystemMessageItem implements Serializable {

    /** 消息 id */
    private Long id;
    /** 消息级别：danger 危险 / warning 预警 */
    private String type;
    /** 消息标题 */
    private String title;
    /** 发生时间 */
    private String time;
    /** 消息正文 */
    private String content;
}
