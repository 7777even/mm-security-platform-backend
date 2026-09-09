package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 台风监测对象实时值。 */
@Data
@TableName("fac_typhoon_monitor_object")
public class FacTyphoonMonitorObject {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属事件 id */
    private Long incidentId;
    /** 对象编码 */
    private String objCode;
    /** 对象名称 */
    private String objName;
    /** 当前监测值 */
    private String objValue;
    /** 计量单位 */
    private String unit;
    /** 状态 */
    private String statusName;
    /** 状态描述 */
    private String statusText;
    /** 排序号 */
    private Integer sortNo;}
