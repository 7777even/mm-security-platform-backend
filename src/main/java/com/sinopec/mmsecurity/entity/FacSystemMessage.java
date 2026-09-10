package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 大屏滚动系统消息（V24 真实表，替代大屏硬编码 systemMessages）。 */
@Data
@TableName("fac_system_message")
public class FacSystemMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String msgType;
    private String title;
    private String content;
    private String occurredAt;
    private Integer sortNo;
}
