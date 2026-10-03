package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备台账写请求（新建/更新共用）。
 *
 * <p>物理主键 deviceCode 为 20 位 MDM 编码，由客户端随请求给定（实体 {@code @TableId(type = IdType.INPUT)}，
 * 不走自增也不走 {@code LedgerIdSupport}）。createdAt / updatedAt / deleted 由服务端维护，不在请求中传递。
 */
@Data
public class DeviceWriteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 20 位 MDM 设备编码，物理主键。 */
    @NotBlank
    private String deviceCode;

    private String deviceName;

    private String deviceType;

    private String zone;

    /** 设备状态（整数枚举，与读端点 status 查询参数同口径）。 */
    private Integer status;

    private Double lat;

    private Double lon;
}
