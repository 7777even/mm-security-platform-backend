package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户信息（GET /auth/me 出参）。
 *
 * <p>前端权限判定的<b>唯一权威来源</b>：{@code perms} 为该用户角色经 sys_role_menu
 * 聚合去重后的权限码全集，前端据此驱动路由守卫与 {@code v-permission}，不再硬编码角色权限表。</p>
 *
 * <p>{@code role} 为兼容既有前端字段保留（单角色，ADR-1）；{@code roles} 为其数组形式，
 * 为将来多角色演进预留（当前恒为长度 1 的列表或空列表）。</p>
 */
@Data
public class MeResult implements Serializable {

    /** 登录用户名 */
    private String username;

    /** 真实姓名（出口按 data-masking 策略脱敏由调用方决定；本域为本人查看，原样返回） */
    private String realName;

    /** 当前角色标识（单角色，如 ADMIN） */
    private String role;

    /** 角色标识列表（当前为长度 1 的列表，预留多角色演进） */
    private List<String> roles;

    /** 权限码全集（由 sys_role_menu 解析，去重） */
    private List<String> perms;

    /** 是否需强制修改口令（true 时前端须跳转改密页） */
    private Boolean mustChangePwd;
}
