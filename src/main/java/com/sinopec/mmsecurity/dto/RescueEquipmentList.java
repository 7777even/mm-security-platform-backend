package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 应急救援资源 - 救援装备列表：中队筛选项 + 装备总套数 + 条目。 */
@Data
public class RescueEquipmentList {
    private List<String> squadrons;
    private Integer totalSets;
    private List<RescueEquipmentItem> items;
}
