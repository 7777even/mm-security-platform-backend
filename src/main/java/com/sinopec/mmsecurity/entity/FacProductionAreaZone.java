package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产应急 - ProductionAreaZone 实体（装置区分区 A/B/C，对应 H2 表 fac_production_area_zone）。
 * zone_code 为分区代码（a/b/c），zone_index 供前端着色与排序。
 */
@Data
@TableName(value = "fac_production_area_zone")
public class FacProductionAreaZone implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long facilityId;

    private String zoneCode;

    private String name;

    private Integer alarmCount;

    private Integer zoneIndex;

    private Integer sortNo;
}
