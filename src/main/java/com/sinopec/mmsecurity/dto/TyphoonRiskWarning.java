package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 风险预警条目，与 typhoon-emergency.openapi.json#/TyphoonRiskWarning 对齐。
 */
@Data
public class TyphoonRiskWarning implements Serializable {

    /** 预警 id */
    private String id;
    /** 发布时间 HH:mm */
    private String time;
    /** 预警类型 */
    private String type;
    /** 预警内容 */
    private String content;
}
