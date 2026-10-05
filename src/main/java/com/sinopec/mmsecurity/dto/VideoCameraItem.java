package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import lombok.Data;

/** 视频控制 - 摄像头画面项（对应前端 VideoControlCell，status: live/loading/ai）。 */
@Data
public class VideoCameraItem implements ZoneAware {
    private Long id;
    private String name;
    private String cameraType;
    private String location;
    private String status;
    private Boolean hd;
    private Integer thumbIndex;

    /**
     * ABAC 实时广播防区收紧的扩展点：暴露位置字段 location（相机安装位置），由 {@code RealtimeSyncAspect}
     * 经 {@code ZoneMappingResolver} 按配置 {@code abac.zone-mapping.location-to-zones} 映射为防区，
     * 注入 {@code EntityChangedEvent.zones}，使写广播按防区过滤（最小权限）。
     * 映射未配置或未命中 → 返回 null → 该域 fail-open（推给全部已认证会话），与既有语义一致。
     */
    @Override
    public String getLocation() {
        return this.location;
    }
}
