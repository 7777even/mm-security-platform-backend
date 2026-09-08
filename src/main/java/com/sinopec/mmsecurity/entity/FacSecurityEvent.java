package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 安防门禁事件实体。数据来自真实表 fac_security_event，由前端 securityEventStore 消费。
 */
@Data
@TableName("fac_security_event")
public class FacSecurityEvent {
    private String eventId;

    /** 乐观锁版本号：MyBatis-Plus @Version，在 update 时自动比对并自增。 */
    @Version
    private Long version;
    private String person;
    private String channel;
    private String cardId;
    private String vehicle;
    private String direction;
    private Integer level;
    private String ts;
}
