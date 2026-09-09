package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 台风监测对象实时值，与 typhoon-emergency.openapi.json#/TyphoonMonitorObject 对齐。
 */
@Data
public class TyphoonMonitorObject implements Serializable {

    /** 监测对象编码 */
    private String id;
    /** 监测对象名称 */
    private String name;
    /** 当前监测值 */
    private String value;
    /** 计量单位 */
    private String unit;
    /** 状态（normal / warning / critical） */
    private String status;
    /** 状态中文描述 */
    private String statusText;
}
