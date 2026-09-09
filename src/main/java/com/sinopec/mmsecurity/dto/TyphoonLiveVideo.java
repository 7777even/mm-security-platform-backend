package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 现场监控视频点位，与 typhoon-emergency.openapi.json#/TyphoonLiveVideo 对齐。
 */
@Data
public class TyphoonLiveVideo implements Serializable {

    /** 视频点位 id */
    private String id;
    /** 点位名称 */
    private String label;
    /** 关联场景序号 */
    private Integer sceneIndex;
    /** 机位角度 */
    private String angle;
    /** 在线状态 online / offline */
    private String status;
    /** 设备编码 */
    private String deviceCode;
}
