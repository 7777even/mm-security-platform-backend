package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施设备整体状态（单行聚合），与前端
 * {@code fire-monitoring.openapi.json#/components/schemas/FireEquipmentStatus} 对齐。
 */
@Data
public class FireEquipmentStatus implements Serializable {

    /** 设备总数 */
    private Integer total;
    /** 离线设备数 */
    private Integer offline;
    /** 故障设备数 */
    private Integer fault;
    /** 完好率百分比整数（0-100） */
    private Integer integrityRate;
    /** 在线率百分比整数（0-100） */
    private Integer onlineRate;
}
