package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 修改用户入参（PUT /system/users/{id}）。口令不在此改（走重置/本人改密端点）。 */
@Data
public class SystemUserUpdate implements Serializable {

    /** 真实姓名 */
    @Size(max = 64, message = "姓名长度不能超过 64")
    private String realName;

    /** 角色标识（须为已启用角色） */
    private String roleCode;

    /** 1 启用 / 0 停用 */
    private Integer status;
}
