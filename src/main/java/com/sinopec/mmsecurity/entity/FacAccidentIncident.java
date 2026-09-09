package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 事故救援 - AccidentIncident 实体（对应 H2 表 fac_accidentincident）。
 */
@Data
@TableName(value = "fac_accident_incident")
public class FacAccidentIncident implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long eventId;

    private String title;

    private String location;

    private Double longitude;

    private Double latitude;

    private String hazardSourceLevel;

    private String mapStatus;

    private String startedAt;

    private String endedAt;

    private String statusName;

    private Boolean reported;

    private String facilityName;

    private Boolean isDefault;
}
