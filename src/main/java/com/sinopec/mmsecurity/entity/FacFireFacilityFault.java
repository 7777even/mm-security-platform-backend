package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施监测 - 故障工单实体（对应 H2 表 fac_fire_facility_fault）。
 * 列后缀 _status/_level/_type 均加域前缀：H2 中 status 等虽非强保留字，仍按工程约定加前缀避免歧义。
 */
@Data
@TableName(value = "fac_fire_facility_fault")
public class FacFireFacilityFault implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String faultCode;

    private String facilityCode;

    private String facilityName;

    private String facilityType;

    private String faultType;

    private String faultLevel;

    private String discoverTime;

    private String discoverMethod;

    private String phenomenon;

    private String causeText;

    private String faultStatus;

    private String workOrderNo;

    private String repairPerson;

    private String estimatedFinish;

    private String actualFinish;

    private String repairMeasures;

    private String acceptancePerson;

    private String acceptanceResult;

    private Integer sortNo;
}
