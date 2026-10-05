package com.sinopec.mmsecurity.entity;

import com.sinopec.mmsecurity.websocket.ZoneAware;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备台账。物理主键 = 20 位 MDM device_code，不自增。
 */
@Data
@TableName("fac_device")
public class FacDevice implements ZoneAware {

    /** 20 位 MDM 编码，物理主键 */
    @TableId(type = IdType.INPUT)
    private String deviceCode;

    private String deviceName;
    private String deviceType;
    private String zone;
    private Integer status;
    private Double lat;
    private Double lon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;

    /** 乐观锁版本（V104 加列，默认 0），供 @Version 与实时广播使用。 */
    @com.baomidou.mybatisplus.annotation.Version
    private Long version;

    /**
     * ABAC 实时广播防区收紧的唯一扩展点：设备直接持有与 {@code sys_zone.zone_name} 对齐的防区名，
     * 供 {@code RealtimeSyncAspect} 注入 {@code EntityChangedEvent.zones}（直达，跳过 location 映射）。
     * 为空时返回 null → 该域 fail-open（推给全部已认证会话），与既有公开语义一致；
     * 非空时仅推送给防区交集命中的已认证会话（最小权限）。取值是否对齐 {@code sys_zone} 由种子/产品映射保证，代码不硬编码。
     */
    @Override
    public String getZoneName() {
        return this.zone;
    }
}
