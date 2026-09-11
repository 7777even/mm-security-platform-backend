package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 消防态势大屏 - 重点监控对象项（对应 fac_fire_monitored_object，V38）。 */
@Data
public class FireMonitoredObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    /** 告警 / 预警 / 正常 */
    private String status;

    private String detail;

    /** danger 危险 / warning 预警 / normal 正常 —— 驱动卡片配色 */
    private String tone;
}
