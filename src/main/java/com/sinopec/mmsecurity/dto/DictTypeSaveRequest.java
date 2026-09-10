package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 新增 / 修改字典类型入参（POST /system/dict-types、PUT /system/dict-types/{id}）。 */
@Data
public class DictTypeSaveRequest implements Serializable {

    /** 字典标识（唯一；保存时归一为小写下划线） */
    @NotBlank(message = "字典标识不能为空")
    @Size(max = 64, message = "字典标识长度不能超过 64")
    private String dictCode;

    /** 字典名称 */
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 64, message = "字典名称长度不能超过 64")
    private String dictName;

    /** 说明 */
    @Size(max = 255, message = "说明长度不能超过 255")
    private String description;

    /** 1 启用 / 0 停用，缺省 1 */
    private Integer status;
}
