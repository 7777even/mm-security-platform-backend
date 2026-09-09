package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 火情态势地图点位（字段与前端 fireSituationMapMock.fireSituationMarkers 对齐）。
 * id 为字符串点位标识（如 event-1），由实体 marker_code 映射；level 可为空。
 */
@Data
public class FireSituationMarkerItem {
    private String id;
    private String kind;
    private String title;
    private String subtitle;
    private Double longitude;
    private Double latitude;
    private Boolean important;
    private String iconUrl;
    private String level;
    private Long targetId;
}
