package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 新增 / 修改字典项入参（POST /system/dict-items、PUT /system/dict-items/{id}）。 */
@Data
public class DictItemSaveRequest implements Serializable {

    /** 所属字典标识（须为已存在的字典类型） */
    @NotBlank(message = "字典标识不能为空")
    @Size(max = 64, message = "字典标识长度不能超过 64")
    private String dictCode;

    /** 字典值 */
    @NotBlank(message = "字典值不能为空")
    @Size(max = 128, message = "字典值长度不能超过 128")
    private String itemValue;

    /** 字典显示名 */
    @NotBlank(message = "字典显示名不能为空")
    @Size(max = 128, message = "字典显示名长度不能超过 128")
    private String itemLabel;

    /** 排序值，缺省 0 */
    private Integer sortOrder;

    /** 1 启用 / 0 停用，缺省 1 */
    private Integer status;

    /** 说明 */
    @Size(max = 255, message = "说明长度不能超过 255")
    private String description;
}
