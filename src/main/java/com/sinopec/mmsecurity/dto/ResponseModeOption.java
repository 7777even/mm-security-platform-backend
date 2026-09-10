package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 应急响应模式选项，与前端 {@code RESPONSE_MODE_OPTIONS} 对齐（契约 #/ResponseModeOption）。 */
@Data
public class ResponseModeOption implements Serializable {

    /** 模式编码（team/plant/company/government） */
    private String value;

    /** 模式名称（一、班组处置 …） */
    private String label;

    /** 该模式对应起始节点号 */
    private Integer stageId;
}
