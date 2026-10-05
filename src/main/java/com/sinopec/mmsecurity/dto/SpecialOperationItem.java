package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

/** 特殊作业 - 作业票列表行（字段名对齐前端 SpecialOperationRecord 标量部分）。 */
@Data
public class SpecialOperationItem implements ZoneAware {
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

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（特殊作业地点），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
