package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 火情态势地图点位聚合：items 为全部点位列表。 */
@Data
public class FireSituationMarkerSummary {
    private List<FireSituationMarkerItem> items;
}
