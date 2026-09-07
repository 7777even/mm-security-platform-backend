package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fac_alarm")
public class FacAlarm {
    private Long id;
    private String deviceCode;
    private Integer level;
    private String type;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
    private Integer deleted;
}
