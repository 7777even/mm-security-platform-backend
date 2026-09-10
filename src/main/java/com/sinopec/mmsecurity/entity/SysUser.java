package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户。
 *
 * <p>{@code role} 为角色标识（引用 {@code sys_role.role_code}，一人一角色 —— 见 ADR-1）。
 * V32 起新增口令生命周期两列：{@code pwd_updated_at}（最近改密时间）、
 * {@code must_change_pwd}（1 = 下次登录强制改密）。</p>
 */
@Data
@TableName("sys_user")
public class SysUser {
    private Long id;
    private String username;
    private String passwordHash;
    private String realName;
    private String role;
    private Integer status;

    /** 最近一次修改口令的时间（可为空：历史账号/从未改密） */
    @TableField("pwd_updated_at")
    private LocalDateTime pwdUpdatedAt;

    /** 1 = 下次登录强制改密（默认账号与管理员重置密码后置 1） */
    @TableField("must_change_pwd")
    private Integer mustChangePwd;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
