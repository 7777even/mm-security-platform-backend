package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 字典类型列表项 / 详情（GET /system/dict-types 出参）。 */
@Data
public class DictTypeItem implements Serializable {

    /** 字典类型 id */
    private Long id;

    /** 字典标识（唯一） */
    private String dictCode;

    /** 字典名称 */
    private String dictName;

    /** 说明 */
    private String description;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 是否内置字典（内置禁删） */
    private Boolean builtIn;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
