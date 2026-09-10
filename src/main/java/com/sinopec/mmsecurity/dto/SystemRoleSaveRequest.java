package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 新增 / 修改角色入参（POST /system/roles、PUT /system/roles/{id}）。 */
@Data
public class SystemRoleSaveRequest implements Serializable {

    /** 角色标识（唯一，保存时统一大写；内置角色禁改） */
    @NotBlank(message = "角色标识不能为空")
    @Size(max = 64, message = "角色标识长度不能超过 64")
    private String roleCode;

    /** 角色中文名 */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称长度不能超过 64")
    private String roleName;

    /** 角色说明 */
    @Size(max = 255, message = "说明长度不能超过 255")
    private String description;

    /** 数据范围（ALL/DEPT/SELF，缺省 SELF） */
    private String dataScope;

    /** 1 启用 / 0 停用，缺省 1 */
    private Integer status;

    /** 排序值，缺省 0 */
    private Integer sortOrder;
}
