package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fac_alarm")
public class FacAlarm {
    private Long id;

    /** 乐观锁版本号：MyBatis-Plus @Version，在 update 时自动比对 WHERE version=? 并自增。 */
    @Version
    private Long version;
    /** 业务展示 ID（如 AE-2026-001），非物理主键；对齐前端 AlarmItem.alarmId */
    private String alarmId;
    private String deviceCode;
    private Integer level;
    private String type;
    private String title;
    private String content;
    /** 后端 int 语义：0=ACTIVE/1=ACKED/2=DISPATCHED/3=CLOSED；由 AlarmAssembler 映射为 string 枚举 */
    private Integer status;
    private LocalDateTime occurredAt;
    private String location;
    private String category;
    private Boolean warned;
    private String planId;
    private LocalDateTime createdAt;
    private Integer deleted;
}
