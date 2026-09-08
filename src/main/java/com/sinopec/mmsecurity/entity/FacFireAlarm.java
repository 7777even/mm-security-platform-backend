package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 消防报警实体。数据来自真实表 fac_fire_alarm（由 FacFireAlarmMapper 查询），
 * 不再返回前端本地 mock。字段对齐前端 {@code FireAlarmItem}（alarmId 为主键，status 为 ACTIVE/CLOSED）。
 */
@Data
@TableName("fac_fire_alarm")
public class FacFireAlarm {
    private String alarmId;

    /** 乐观锁版本号：MyBatis-Plus @Version，在 update 时自动比对并自增。 */
    @Version
    private Long version;
    private String typeLabel;
    private String typeTone;
    private String source;
    private String objectType;
    private String objectName;
    private String level;
    private String description;
    private String location;
    private String time;
    private String falseAlarm;
    private String status;
    private String rescueEventId;
    private String monitorId;
    private String monitorLabel;
    private String onsiteMonitorId;
    private String onsiteMonitorLabel;
    private String title;
}
