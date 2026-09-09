package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 装置区分区（对应前端 ProductionAreaZone）。id 为分区代码 a/b/c，zoneIndex 供着色与排序。 */
@Data
public class ProductionAreaZone implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Integer alarmCount;
    private Integer zoneIndex;
}
