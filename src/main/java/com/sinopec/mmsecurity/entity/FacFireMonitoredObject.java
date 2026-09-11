package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消防态势大屏 - 重点监控对象实体（对应 H2 表 fac_fire_monitored_object，V38）。
 *
 * <p>取代前端 FireMonitoredObjectsPanel 硬编码的 4 个重点监控对象（status/detail/tone）。
 */
@Data
@TableName(value = "fac_fire_monitored_object")
public class FacFireMonitoredObject implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String objName;

    /** 告警 / 预警 / 正常 */
    private String status;

    private String detail;

    /** danger 危险 / warning 预警 / normal 正常 —— 驱动卡片配色 */
    private String tone;

    private Integer sortNo;
}
