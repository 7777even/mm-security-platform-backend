package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 通讯设备写请求（新建/更新共用）。
 *
 * <p>id 与 sort_no 由服务端按 {@code LedgerIdSupport} 分配；deviceCode 为业务自然键
 * （与 GET /communication/devices/{id} 详情一致）。longitude/latitude 为 DOUBLE。
 */
@Data
public class CommDeviceWriteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String deviceCode;

    @NotBlank
    private String deviceType;

    private String groupKey;

    private String groupLabel;

    @NotBlank
    private String deviceName;

    private String areaName;

    private String locationName;

    private String deviceStatus;

    private Double longitude;

    private Double latitude;

    private String categoryName;

    private String installTime;

    private String ownerName;

    private String ipAddress;

    private String lastCheckTime;
}
