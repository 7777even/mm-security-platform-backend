package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 周界入侵告警实体（真实数据源，替代前端 demoAlarmDetails 中的 demo-intrusion-1）。
 * 字段与前端 AlarmDetailItem 视图模型一一对应，typeFields（入侵位置/方式/关联摄像机）
 * 由告警类型字段在前端适配器中组装，避免把展示态存库。
 */
@Data
@TableName("fac_perimeter_alarm")
public class FacPerimeterAlarm {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String alarmCode;
    private String title;
    private String alarmType;
    private String source;
    /** 告警级别（一级/二级/三级），列名 level_code 规避 SQL 方言差异 */
    private String levelCode;
    private String status;
    private String falseAlarm;
    private String alarmTime;
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
    /** 派发人员，逗号分隔（中文逗号/英文逗号均可） */
    private String dispatchPersonnel;
    private Boolean notifyApp;
    private Boolean notifySms;
    private String handleResult;
    private String handleTime;
    private Long rescueEventId;
    private String monitorId;
    private String monitorLabel;
    private String workOrderNo;
    /** 现场抓拍（dev 由 PerimeterAlarmSnapshotSeeder 生成占位 JPEG；接真流后由媒体网关写入） */
    private byte[] snapshotBytes;
}
