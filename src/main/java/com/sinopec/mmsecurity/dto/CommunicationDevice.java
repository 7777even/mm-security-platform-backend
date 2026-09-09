package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 通讯设备 - 单台设备（id 为设备编码字符串，如 'bc-a1'）。
 * 字段名与前端 mock 的 CommunicationDevice 完全一致。
 */
@Data
public class CommunicationDevice {
    private String id;
    private String type;
    private String name;
    private String area;
    private String location;
    private String status;
    private Double longitude;
    private Double latitude;
    private CommunicationDeviceDetail detail;
}
