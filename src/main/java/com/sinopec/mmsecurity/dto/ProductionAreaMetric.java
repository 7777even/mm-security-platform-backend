package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 装置区指标项（对应前端 ProductionAreaMetric）。value 为已格式化的展示串。 */
@Data
public class ProductionAreaMetric implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String label;
    private String value;
}
