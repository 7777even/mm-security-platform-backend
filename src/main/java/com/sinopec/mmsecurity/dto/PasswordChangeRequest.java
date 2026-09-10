package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/** 本人修改口令入参（POST /auth/password）。须校验旧口令，防止会话被劫持后直接改密。 */
@Data
public class PasswordChangeRequest implements Serializable {

    /** 旧口令（须与库中哈希匹配） */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新口令（服务端按策略校验：长度 / 字符类别 / 不含用户名 / 不与旧口令相同） */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
