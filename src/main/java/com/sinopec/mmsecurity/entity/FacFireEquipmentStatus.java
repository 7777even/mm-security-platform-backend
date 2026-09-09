package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 消防设施设备整体状态（单行聚合，替代大屏硬编码 equipmentStatus）。 */
@Data
@TableName("fac_fire_equipment_status")
public class FacFireEquipmentStatus {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer totalCnt;
    private Integer offlineCnt;
    private Integer faultCnt;
    /** 完好率百分比整数（0-100） */
    private Integer integrityRate;
    /** 在线率百分比整数（0-100） */
    private Integer onlineRate;
}
