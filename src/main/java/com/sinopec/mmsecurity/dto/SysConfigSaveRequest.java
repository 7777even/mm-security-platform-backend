package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 新增 / 修改系统参数配置入参（POST /system/configs、PUT /system/configs/{id}）。 */
@Data
public class SysConfigSaveRequest implements Serializable {

    /** 配置键（唯一，如 system.title / abac.zone-mapping.mode） */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 128, message = "配置键长度不能超过 128")
    private String configKey;

    /** 配置值（按 config_type 解释） */
    @Size(max = 2000, message = "配置值长度不能超过 2000")
    private String configValue;

    /** 显示名称 */
    @Size(max = 128, message = "显示名称长度不能超过 128")
    private String configName;

    /** 分组（如 system / abac / alarm） */
    @Size(max = 64, message = "分组长度不能超过 64")
    private String configGroup;

    /** 类型：STRING / NUMBER / BOOLEAN / JSON / SELECT */
    @Size(max = 16, message = "类型长度不能超过 16")
    private String configType;

    /** SELECT 类型的候选项（JSON 数组或逗号分隔） */
    @Size(max = 500, message = "选项长度不能超过 500")
    private String options;

    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;

    /** 排序，缺省 0 */
    private Integer sortOrder;

    /** 1 启用 / 0 停用，缺省 1 */
    private Integer status;
}
