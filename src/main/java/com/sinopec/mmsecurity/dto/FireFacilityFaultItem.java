package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 故障工单条目（含派单/维修/验收字段与故障时间线子表）。 */
@Data
public class FireFacilityFaultItem implements Serializable {
    private static final long serialVersionUID = 1L;

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
    private String cause;
    private String status;
    private String workOrderNo;
    private String repairPerson;
    private String estimatedFinish;
    private String actualFinish;
    private String repairMeasures;
    private String acceptancePerson;
    private String acceptanceResult;
    private List<FireFacilityFaultTimeline> timeline;
}
