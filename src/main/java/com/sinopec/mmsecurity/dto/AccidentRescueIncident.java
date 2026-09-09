package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 事故救援事件聚合（对应前端 AccidentRescueIncident）。
 * 由 fac_accident_incident 主表 + 五类子表组装而成。
 */
@Data
public class AccidentRescueIncident implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long eventId;
    private String title;
    private String location;
    private Double longitude;
    private Double latitude;
    private String hazardSourceLevel;
    private String mapStatus;
    private String startedAt;
    private String endedAt;
    private String status;
    private Boolean reported;
    private String facilityName;

    private List<IncidentDetailField> detailFields = new ArrayList<>();
    private List<EmergencyDispatchResource> dispatchResources = new ArrayList<>();
    private List<RescueDutyPerson> dutyPersons = new ArrayList<>();
    private List<RescueAuxiliaryStat> auxiliaryStats = new ArrayList<>();
    private List<RescueDynamicEntry> dynamics = new ArrayList<>();
}
