package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 特殊作业 - 气体检测点（对应前端 SpecialOperationGasPoint）。 */
@Data
public class SpecialOperationGasPoint {
    private Long id;
    private String name;
    private String value;
    private String status;
}
