package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消防态势大屏 - 装置区保障汇总实体（对应 H2 表 fac_fire_monitor_area，V38）。
 *
 * <p>取代前端 SafetyAlarmPanel 硬编码的 14 个装置区（equipment/cameras/personnel/status）。
 */
@Data
@TableName(value = "fac_fire_monitor_area")
public class FacFireMonitorArea implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 前端区域编码（refinery-1 / chemical-3 / port-2 ...），即契约里的 id */
    private String areaCode;

    /** 厂区分类：refinery 炼油 / chemical 化工 / port 港口 */
    private String scope;

    private String areaName;

    /** normal 正常 / attention 关注 */
    private String status;

    private String statusLabel;

    private Integer equipment;

    private Integer cameras;

    private Integer personnel;

    private Integer sortNo;
}
