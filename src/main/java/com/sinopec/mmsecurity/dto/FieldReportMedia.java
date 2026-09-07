package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 现场采集回传附件媒体，与前端 {@code uplink.openapi.json#/FieldReportMedia} 对齐。
 */
@Data
public class FieldReportMedia implements Serializable {

    /** 媒体类型 image / video */
    private String type;
    /** 文件名 */
    private String name;
    /** 字节大小 */
    private Integer size;
}
