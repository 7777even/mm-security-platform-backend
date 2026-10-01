package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 消防报警全字段更新请求（局部更新，仅传入需变更的字段，未传不更新）。
 * 向后兼容：大屏 AlarmDetailPanel 仅传 status/falseAlarm/handle* 6 字段，不影响。
 * 枚举约束对齐字典 fire_alarm_status / fire_alarm_false。
 */
@Data
public class FireAlarmUpdateRequest {
    private String typeLabel;     // 类型标签（火灾报警/烟雾报警/GDS报警/设备故障）
    private String typeTone;      // 类型色调（fire/smoke/gds/muted）
    private String source;        // 报警来源
    private String objectType;    // 对象类型（装置/储罐/仓库/管网）
    private String objectName;    // 对象名称
    private String level;         // 报警等级
    private String description;   // 报警描述
    private String location;      // 报警位置
    private String time;          // 报警时间
    private String falseAlarm;    // 是否误报：是/否/未核实
    private String status;        // 处置状态：ACTIVE/ACKED/DISPATCHED/CLOSED
    private String rescueEventId; // 关联救援事件 id
    private String monitorId;     // 监控点 id
    private String monitorLabel;  // 监控点名称
    private String onsiteMonitorId;     // 现场监控点 id
    private String onsiteMonitorLabel;  // 现场监控点名称
    private String title;         // 报警标题
    private String handleResult;  // 处置情况文本
    private String handleTime;    // 处置时间（yyyy-MM-dd HH:mm:ss）
    private String dispatchPersonnel; // 派单人员（英文逗号分隔）
    private String notifyMethod;  // 通知方式（APP/SMS）
}
