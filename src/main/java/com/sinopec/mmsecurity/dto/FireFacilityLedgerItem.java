package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防设施监测 - 设施台账条目（含维保记录子表）。 */
@Data
public class FireFacilityLedgerItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String facilityCode;
    private String facilityName;
    private String facilityType;
    private String location;
    private String device;
    private String maintainerName;
    private String maintainerPhone;
    private Boolean enabled;
    private List<FireFacilityMaintenanceRecord> maintenanceRecords;
}
