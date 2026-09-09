package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 应急救援资源 - 救援车辆列表：中队/类型筛选项 + 条目。 */
@Data
public class RescueVehicleList {
    private List<String> squadrons;
    private List<String> types;
    private List<RescueVehicleItem> items;
}
