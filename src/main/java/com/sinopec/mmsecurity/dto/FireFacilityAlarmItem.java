package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 消防设施监测 - 报警条目（由故障工单派生：id=AL-<故障号数字部分>，category：火灾|故障|动作）。 */
@Data
public class FireFacilityAlarmItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String source;
    private String facilityType;
    private String level;
    private String category;
    private String content;
    private String time;
    private String status;
    private String faultCode;
}
