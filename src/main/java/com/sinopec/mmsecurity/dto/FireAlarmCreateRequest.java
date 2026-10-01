package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 消防报警新增请求：业务事实字段 + 处置字段。
 * 必填：title（报警名称）、time（报警时间）；其余可选；status 不传默认 ACTIVE。
 */
@Data
public class FireAlarmCreateRequest {
    @NotBlank(message = "报警名称(title)不能为空")
    private String title;
    @NotBlank(message = "报警时间(time)不能为空")
    private String time;
    private String typeLabel;
    private String typeTone;
    private String source;
    private String objectType;
    private String objectName;
    private String level;
    private String description;
    private String location;
    private String falseAlarm;
    private String status;        // 不传默认 ACTIVE
    private String rescueEventId;
    private String monitorId;
    private String monitorLabel;
    private String onsiteMonitorId;
    private String onsiteMonitorLabel;
    private String handleResult;
    private String handleTime;
    private String dispatchPersonnel;
    private String notifyMethod;
}
