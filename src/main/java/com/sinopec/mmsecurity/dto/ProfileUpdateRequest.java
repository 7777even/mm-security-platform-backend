package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 本人资料修改入参（PUT /auth/profile）。仅允许改姓名，角色/状态不可自改（防自我提权）。 */
@Data
public class ProfileUpdateRequest implements Serializable {

    /** 真实姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过 64")
    private String realName;
}
