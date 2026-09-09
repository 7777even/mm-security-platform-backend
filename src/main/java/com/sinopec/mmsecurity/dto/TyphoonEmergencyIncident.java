package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 台风应急事件聚合，与 typhoon-emergency.openapi.json#/TyphoonEmergencyIncident 对齐。
 */
@Data
public class TyphoonEmergencyIncident implements Serializable {

    /** 应急事件 id */
    private Long eventId;
    /** 事件标题 */
    private String title;
    /** 影响位置 */
    private String location;
    /** 事件经度 */
    private Double longitude;
    /** 事件纬度 */
    private Double latitude;
    /** 启动时间 */
    private String startedAt;
    /** 结束时间，未结束为空 */
    private String endedAt;
    /** 处置状态 processing / pending / done */
    private String status;
    /** 气象概要 */
    private String meteorologySummary;
    /** 水位预警线 */
    private Double waterLevelWarn;
    /** 水位警戒线 */
    private Double waterLevelDanger;
    /** 台风编号 */
    private String typhoonApiCode;
    /** 降雨 / 风速图表横轴标签（默认空集合，避免前端空指针） */
    private List<String> weatherChartLabels = new ArrayList<>();
    /** 逐小时降雨量序列（默认空集合，避免前端空指针） */
    private List<Double> precipitationSeries = new ArrayList<>();
    /** 逐小时风速序列（默认空集合，避免前端空指针） */
    private List<Double> windSpeedSeries = new ArrayList<>();
    /** 水位图表横轴标签（默认空集合，避免前端空指针） */
    private List<String> waterLevelLabels = new ArrayList<>();
    /** 水位序列（默认空集合，避免前端空指针） */
    private List<Double> waterLevelSeries = new ArrayList<>();
    /** 监测对象（默认空集合，避免前端空指针） */
    private List<TyphoonMonitorObject> monitoringObjects = new ArrayList<>();
    /** 风险预警（默认空集合，避免前端空指针） */
    private List<TyphoonRiskWarning> riskWarnings = new ArrayList<>();
    /** 现场视频（默认空集合，避免前端空指针） */
    private List<TyphoonLiveVideo> liveVideos = new ArrayList<>();
    /** 知识库条目（默认空集合，避免前端空指针） */
    private List<TyphoonAuxItem> auxiliaryItems = new ArrayList<>();
    /** 值班人员（默认空集合，避免前端空指针） */
    private List<TyphoonDutyPerson> dutyPersons = new ArrayList<>();
    /** 易涝点位（默认空集合，避免前端空指针） */
    private List<TyphoonMapRiskPoint> mapRiskPoints = new ArrayList<>();
    /** 事件信息字段（默认空集合，避免前端空指针） */
    private List<TyphoonEventInfoField> eventInfoFields = new ArrayList<>();
}
