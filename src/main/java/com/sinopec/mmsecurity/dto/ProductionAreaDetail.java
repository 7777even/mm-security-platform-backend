package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 装置区二级页聚合详情（对应前端 ProductionAreaDetail）。
 * facilityId 未命中设施表时由 Service 返回 null，Controller 转 404 业务异常。
 */
@Data
public class ProductionAreaDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long facilityId;
    private String facilityName;
    private List<ProductionAreaZone> zones = new ArrayList<>();
    private List<ProductionAreaMetric> metrics = new ArrayList<>();
    private Integer personnelTotal;
    private List<PersonnelSlice> personnelSlices = new ArrayList<>();
    private List<ProductionAlarmItem> alarms = new ArrayList<>();
}
