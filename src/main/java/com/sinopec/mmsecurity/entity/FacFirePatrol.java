package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 防火巡查记录（一次班次巡查，替代大屏硬编码 firePatrolRecords）。 */
@Data
@TableName("fac_fire_patrol")
public class FacFirePatrol {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String patrolDate;
    private String shiftName;
    private String dutyPerson;
    private String patrolCount;
    /** 巡查部位，逗号分隔存储（locations 为展示用列表，不参与检索） */
    private String locations;
    private Boolean completed;
    private String workOrderNo;
}
