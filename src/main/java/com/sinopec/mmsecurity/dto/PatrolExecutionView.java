package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 巡更执行记录视图：落库后的完整行，含服务端填充的 {@code operator} / 时间戳。
 *
 * <p>契约 {@code fire-monitoring.openapi.json#/components/schemas/PatrolExecutionView}。</p>
 */
@Data
public class PatrolExecutionView implements Serializable, ZoneAware {

    private Long id;
    private String patrolDate;
    private String shiftName;
    private String dutyPerson;
    private String patrolCount;
    private String location;
    private String execResult;
    private String finding;
    private String workOrderNo;
    private String operator;
    private LocalDateTime createdAt;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（巡更执行位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
