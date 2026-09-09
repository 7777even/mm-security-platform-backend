package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 台风时间序列点（降雨 / 风速 / 水位统一存储，seriesKey 区分）。 */
@Data
@TableName("fac_typhoon_series")
public class FacTyphoonSeries {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属事件 id */
    private Long incidentId;
    /** 序列键 */
    private String seriesKey;
    /** 横轴标签 */
    private String pointLabel;
    /** 数值 */
    private Double pointValue;
    /** 排序号 */
    private Integer sortNo;}
