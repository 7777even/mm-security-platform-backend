package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 3D 地图红色区块信息牌（前端 MaomingPetroCesiumMap 的 plantZonePopups 项，V42）。 */
@Data
public class MapZoneSignPopup {
    private String title;

    /** 位置说明 */
    private String location;

    /** 状态文案 */
    private String status;

    /** 状态强调级别：alert / normal */
    private String statusLevel;
}
