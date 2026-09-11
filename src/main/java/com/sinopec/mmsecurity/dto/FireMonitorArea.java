package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 消防态势大屏 - 装置区保障汇总项（对应 fac_fire_monitor_area，V38）。 */
@Data
public class FireMonitorArea implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 区域编码（refinery-1 ...），前端 key */
    private String id;

    /** 厂区分类：refinery 炼油 / chemical 化工 / port 港口 */
    private String scope;

    private String name;

    /** normal 正常 / attention 关注 */
    private String status;

    private String statusLabel;

    private Integer equipment;

    private Integer cameras;

    private Integer personnel;
}
