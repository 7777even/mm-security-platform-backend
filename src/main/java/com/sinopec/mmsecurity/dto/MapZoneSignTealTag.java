package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 3D 地图青色信息牌（前端 MaomingPetroCesiumMap 的 plantZoneTealTags 项，V42）。 */
@Data
public class MapZoneSignTealTag {
    private String title;

    /** 状态文案 */
    private String status;

    /** 数值文案（如 85%） */
    private String value;
}
