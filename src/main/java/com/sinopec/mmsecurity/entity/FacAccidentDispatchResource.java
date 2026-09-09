package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 事故救援 - AccidentDispatchResource 实体（对应 H2 表 fac_accidentdispatchresource）。
 */
@Data
@TableName(value = "fac_accident_dispatch_resource")
public class FacAccidentDispatchResource implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String resourceCode;

    private String resourceType;

    private String resourceName;

    private String code;

    private String organization;

    private String area;

    private String statusName;

    private Double distanceKm;

    private Integer etaMinutes;

    private String capacity;

    private String contact;

    private String phone;

    private Double longitude;

    private Double latitude;

    private Integer sortNo;
}
