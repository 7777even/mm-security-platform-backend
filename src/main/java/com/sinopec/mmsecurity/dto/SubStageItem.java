package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 节点子阶段项，与前端 {@code SubStageItem} 对齐（契约 #/SubStageItem）。 */
@Data
public class SubStageItem implements Serializable {

    /** 子阶段 id */
    private String id;

    /** 子阶段编码（如 5.1） */
    private String code;

    /** 子阶段名称 */
    private String name;

    /** 子阶段简称 */
    private String shortName;

    /** 子阶段说明 */
    private String description;
}
