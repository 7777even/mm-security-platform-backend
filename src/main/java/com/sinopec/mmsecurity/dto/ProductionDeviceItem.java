package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 设备明细（对应前端 ProductionDeviceItem）。category 与总览设备分类卡片名称一致，便于抽屉标题复用。 */
@Data
public class ProductionDeviceItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String type;
    private String category;
    private String area;
    private String status;
    private Double longitude;
    private Double latitude;
}
