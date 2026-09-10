package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 周界入侵告警详情，契约源：前端 PerimeterAlarmDetail。
 * 前端由 perimeterAlarmToDetail 适配为 AlarmDetailItem（组装 typeFields / timeline / images）。
 */
@Data
public class PerimeterAlarmDetail {

    private Long id;
    private String alarmCode;
    private String title;
    private String alarmType;
    private String source;
    private String level;
    private String status;
    private String falseAlarm;
    private String time;
    private String objectType;
    private String objectName;
    private String location;
    private String description;
    private String deviceType;
    private String deviceId;
    private String point;
    private String intrusionPosition;
    private String intrusionMethod;
    private String relatedCamera;
    private Double longitude;
    private Double latitude;
    private List<String> dispatchPersonnel;
    private Boolean notifyApp;
    private Boolean notifySms;
    private String handleResult;
    private String handleTime;
    private Long rescueEventId;
    private String monitorId;
    private String monitorLabel;
    private String workOrderNo;
    /** 现场抓拍字节端点相对路径（需带 JWT 拉取，前端转 objectURL 后渲染） */
    private String snapshotPath;
    /** 现场抓拍说明文字，无抓拍时为空串 */
    private String snapshotLabel;
}
