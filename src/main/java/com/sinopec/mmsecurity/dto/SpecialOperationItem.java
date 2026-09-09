package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 特殊作业 - 作业票列表行（字段名对齐前端 SpecialOperationRecord 标量部分）。 */
@Data
public class SpecialOperationItem {
    private Long id;
    private String area;
    private String type;
    private String level;
    private String status;
    private String startTime;
    private String endTime;
    private String timeRange;
    private String unit;
    private String applyUnit;
    private String operationDate;
    private String location;
    private String isContractor;
    private String hazardType;
    private String leaderName;
    private String leaderPhone;
    private String position;
    private Double longitude;
    private Double latitude;
    private String changeReason;
    private String cancelReason;
    private String guardianName;
    private String workers;
    private String permitNo;
    private String content;
    private Integer videoCount;
    private Integer gasMonitorCount;
    private Integer personnelCount;
}
