package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 新增用户入参（POST /system/users）。 */
@Data
public class SystemUserCreate implements Serializable {

    /** 登录用户名（唯一） */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过 64")
    private String username;

    /** 初始口令（服务端按复杂度策略校验后 BCrypt 存储，绝不明文落库） */
    @NotBlank(message = "初始密码不能为空")
    private String password;

    /** 真实姓名 */
    @Size(max = 64, message = "姓名长度不能超过 64")
    private String realName;

    /** 角色标识（须为已启用角色） */
    @NotBlank(message = "角色不能为空")
    private String roleCode;

    /** 1 启用 / 0 停用，缺省 1 */
    private Integer status;
}
