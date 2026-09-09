package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 通讯设备 - 设备档案明细挂接信息。
 * 字段名与前端 mock 的 CommunicationDevice.detail 完全一致。
 */
@Data
public class CommunicationDeviceDetail {
    private String category;
    private String installTime;
    private String owner;
    private String ip;
    private String lastCheck;
}
