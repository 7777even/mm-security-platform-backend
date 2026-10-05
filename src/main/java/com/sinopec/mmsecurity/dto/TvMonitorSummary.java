package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

import java.io.Serializable;

/**
 * 工业电视监控点位摘要（设备下拉/筛选用），与前端
 * {@code tv.openapi.json#/components/schemas/TvMonitorSummary} 对齐。
 */
@Data
public class TvMonitorSummary implements Serializable, ZoneAware {

    /** 点位编码 */
    private String code;
    /** 监控名称 */
    private String name;
    /** 是否在线 */
    private Boolean online;
    /** 责任部门 */
    private String department;
    /** 防区编码（关联 sys_zone.zone_code，V86 建立防区维度） */
    private String zoneCode;
    /** 防区名称（由 zone_code 解析） */
    private String zoneName;
    /** 监控分类 code（V87）：PRODUCTION/BOUNDARY/CLOSED_GATE/OTHER_GATE/OTHER，空表示未分类 */
    private String monitorCategory;

    /**
     * ABAC 实时广播防区收紧的扩展点：本对象直接持有与 {@code sys_zone.zone_name} 对齐的防区名（字段 zoneName，工业电视监控设备所属防区），
     * 由 {@code RealtimeSyncAspect} 直达注入 {@code EntityChangedEvent.zones}（跳过 location 映射），
     * 使写广播按防区过滤（最小权限）。为空时该域 fail-open，与既有语义一致。
     */
    @Override
    public String getZoneName() {
        return this.zoneName;
    }
}
