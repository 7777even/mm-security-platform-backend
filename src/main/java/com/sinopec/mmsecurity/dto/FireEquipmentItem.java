package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消防设备分类项，与前端
 * {@code fire-monitoring.openapi.json#/components/schemas/FireEquipmentItem} 对齐。
 */
@Data
public class FireEquipmentItem implements Serializable {

    /** 分类 id */
    private Long id;
    /** 设备分类名 */
    private String name;
    /** 该分类设备数量 */
    private Integer count;
}
