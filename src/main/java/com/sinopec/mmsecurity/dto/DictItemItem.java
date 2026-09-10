package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 字典项（GET /system/dict-items、GET /system/dicts/{dictCode} 出参）。 */
@Data
public class DictItemItem implements Serializable {

    /** 字典项 id */
    private Long id;

    /** 所属字典标识 */
    private String dictCode;

    /** 字典值 */
    private String itemValue;

    /** 字典显示名 */
    private String itemLabel;

    /** 排序值 */
    private Integer sortOrder;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 说明 */
    private String description;
}
