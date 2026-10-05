package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

import java.io.Serializable;

/**
 * 生产报警项（对应前端 ProductionAlarmItem）。
 * 大屏报警面板与装置区二级页共用；装置区场景下 location / description 由服务端按设施名重写。
 */
@Data
public class ProductionAlarmItem implements Serializable, ZoneAware {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String titleColor;
    private String location;
    private String time;
    private String description;
    private String status;
    /** 是否误报：是 / 否 / 未核实（可空，写回后回填）。 */
    private String falseAlarm;
    /** 处置情况文本（写回后回填）。 */
    private String handleResult;
    /** 处置时间（yyyy-MM-dd HH:mm:ss，写回后回填）。 */
    private String handleTime;
    /** 派单人员（逗号分隔，写回后回填）。 */
    private String dispatchPersonnel;
    /** 通知方式（APP/SMS，逗号分隔，写回后回填）。 */
    private String notifyMethod;
    private Integer iconIndex;
    private String thumb;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（生产报警位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
