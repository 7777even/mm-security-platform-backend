package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 工业电视 - 运行统计（对应前端 videoOperationStats + eventTotal）。 */
@Data
public class TvOperationStats {
    private Integer total;
    private Integer offline;
    private Integer fault;
    private Integer integrityRate;
    private Integer onlineRate;
    private Integer eventTotal;
}
