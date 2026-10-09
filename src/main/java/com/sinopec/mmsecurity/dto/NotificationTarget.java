package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 通知关联的业务对象（点击可下钻到对应详情/处置页）。 */
@Data
public class NotificationTarget implements Serializable {
    /** 业务类型：alarm / event / task */
    private String type;
    /** 业务对象 ID */
    private String id;
}
