package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 特殊作业分项统计，与前端
 * {@code fire-monitoring.openapi.json#/components/schemas/SpecialOperationStat} 对齐。
 */
@Data
public class SpecialOperationStat implements Serializable {

    /** 作业类型 id（主键） */
    private Long id;
    /** 作业类型名称（动火作业 / 盲板抽堵 / 吊装作业 / 动土作业 / 受限空间 / 高处作业 / 临时用电 / 断路作业） */
    private String label;
    /** 当前在建数量 */
    private Integer count;
}
