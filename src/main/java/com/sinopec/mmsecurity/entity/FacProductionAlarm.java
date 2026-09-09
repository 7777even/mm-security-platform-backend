package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产应急 - ProductionAlarm 实体（生产报警，对应 H2 表 fac_production_alarm）。
 * facility_id 为空表示全厂级报警；装置区二级页按 facility_id 取本装置区报警并重写 location。
 */
@Data
@TableName(value = "fac_production_alarm")
public class FacProductionAlarm implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long facilityId;

    private String title;

    private String titleColor;

    private String location;

    private String occurredAt;

    private String description;

    private String statusName;

    private Integer iconIndex;

    private String thumb;

    private Integer sortNo;
}
