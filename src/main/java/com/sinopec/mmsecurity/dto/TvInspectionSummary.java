package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 工业电视 - 入厂巡检聚合：车辆列表 + 人员列表（对应前端 inspectionVehicles / inspectionPersons）。 */
@Data
public class TvInspectionSummary {
    private List<TvInspectionItem> vehicles;
    private List<TvInspectionItem> persons;
}
