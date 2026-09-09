package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 事件信息字段，与 typhoon-emergency.openapi.json#/TyphoonEventInfoField 对齐。
 */
@Data
public class TyphoonEventInfoField implements Serializable {

    /** 字段名 */
    private String label;
    /** 字段值 */
    private String value;
}
