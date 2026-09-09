package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 维修工单条目（由已派单的故障工单派生）。 */
@Data
public class FireFacilityWorkOrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String workOrderNo;
    private String faultCode;
    private String facilityCode;
    private String facilityName;
    private String facilityType;
    private String faultLevel;
    private String description;
    private String status;
    private String dispatchTime;
    private String repairPerson;
    private String estimatedFinish;
    private String actualFinish;
    private List<FireFacilityFaultTimeline> timeline;
}
