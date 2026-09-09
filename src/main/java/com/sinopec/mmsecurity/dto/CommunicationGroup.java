package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 通讯设备 - 单个分组（如「A装置区 (6)」）。
 * 字段名与前端 communicationDeviceMock.ts 的 CommunicationGroup 完全一致。
 */
@Data
public class CommunicationGroup {
    private String key;
    private String label;
    private List<CommunicationDevice> devices;
}
