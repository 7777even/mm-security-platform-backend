package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 台风事件信息字段（键值对）。 */
@Data
@TableName("fac_typhoon_event_info")
public class FacTyphoonEventInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属事件 id */
    private Long incidentId;
    /** 字段名 */
    private String fieldLabel;
    /** 字段值 */
    private String fieldValue;
    /** 排序号 */
    private Integer sortNo;}
