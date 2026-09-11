package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 3D 地图装置区信息牌聚合（取代前端 MAP_THEME 内硬编码文案，V42）。 */
@Data
public class MapZoneSigns {
    private List<MapZoneSignPopup> popups;

    private List<MapZoneSignTealTag> tealTags;
}
