package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施监测 - 设施台账实体（对应 H2 表 fac_fire_facility_ledger）。
 * enabled_flag：是否启用。
 */
@Data
@TableName(value = "fac_fire_facility_ledger")
public class FacFireFacilityLedger implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String facilityCode;

    private String facilityName;

    private String facilityType;

    private String locationName;

    private String deviceName;

    private String maintainerName;

    private String maintainerPhone;

    private Boolean enabledFlag;

    private Integer sortNo;
}
