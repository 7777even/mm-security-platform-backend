package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 台风应急事件主记录（真实数据源，替代大屏硬编码 defaultTyphoonIncident）。 */
@Data
@TableName("fac_typhoon_incident")
public class FacTyphoonIncident {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联应急事件 id */
    private Long eventId;
    /** 事件标题 */
    private String title;
    /** 影响位置 */
    private String location;
    /** 事件经度 */
    private Double longitude;
    /** 事件纬度 */
    private Double latitude;
    /** 启动时间 */
    private String startedAt;
    /** 结束时间，未结束为空 */
    private String endedAt;
    /** 处置状态 */
    private String statusName;
    /** 气象概要 */
    private String meteorologySummary;
    /** 水位预警线 */
    private Double waterLevelWarn;
    /** 水位警戒线 */
    private Double waterLevelDanger;
    /** 台风编号 */
    private String typhoonApiCode;
    /** 是否默认防台防汛事件 */
    private Boolean isDefault;}
