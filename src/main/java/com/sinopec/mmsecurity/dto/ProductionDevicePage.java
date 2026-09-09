package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 设备分页结果（对应前端 ProductionDevicePage）。page 从 1 开始，total 为过滤后的总条数。 */
@Data
public class ProductionDevicePage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page;
    private Integer size;
    private Long total;
    private List<ProductionDeviceItem> items = new ArrayList<>();
}
