package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 道闸（门禁卡口）列表项 DTO —— 与前端 {@code security.openapi.json#/GateControlItem} 字节级对齐。
 * 数据来自真实表 fac_gate_control。
 */
@Data
public class GateControlItem {
    private Long id;
    private String name;
    private String location;
    private String status;
    private Double longitude;
    private Double latitude;
}
