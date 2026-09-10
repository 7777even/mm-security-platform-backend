package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员重置口令结果（POST /system/users/{id}/password/reset 出参）。
 * 临时口令为一次性返回（仅此一次可见），用户下次登录须强制改密。
 */
@Data
public class PasswordResetResult implements Serializable {

    /** 服务端随机生成的临时口令（不得为固定值） */
    private String temporaryPassword;

    /** 是否需强制修改口令（重置后恒为 true） */
    private Boolean mustChangePwd;
}
