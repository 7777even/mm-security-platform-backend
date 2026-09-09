package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 工业电视 - 入厂巡检记录项（车辆 kind=VEHICLE：name 为空、plate 有值；
 * 人员 kind=PERSON：plate 为空、name/department 有值）。
 */
@Data
public class TvInspectionItem {
    private Long id;
    private String kind;
    private String areaCode;
    private String plate;
    private String name;
    private String badge;
    private String department;
    private String gate;
    private String time;
}
