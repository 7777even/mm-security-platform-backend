package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

/**
 * 防恐柱列表项 DTO —— 与前端 {@code security.openapi.json#/BollardItem} 字节级对齐。
 * 数据来自真实表 fac_bollard。
 */
@Data
public class BollardItem implements ZoneAware {
    private Long id;
    private String name;
    private String zone;
    private String status;
    private Double longitude;
    private Double latitude;

    /**
     * ABAC 实时广播防区收紧的扩展点：本对象直接持有与 {@code sys_zone.zone_name} 对齐的防区名（字段 zone，防恐柱所属防区），
     * 由 {@code RealtimeSyncAspect} 直达注入 {@code EntityChangedEvent.zones}（跳过 location 映射），
     * 使写广播按防区过滤（最小权限）。为空时该域 fail-open，与既有语义一致。
     */
    @Override
    public String getZoneName() {
        return this.zone;
    }
}
