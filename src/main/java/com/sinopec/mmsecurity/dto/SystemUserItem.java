package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户列表项 / 详情（GET /system/users 系列出参）。
 *
 * <p>绝不包含 {@code passwordHash}（见 password-security.md 反模式）。</p>
 *
 * <p><b>realName 不脱敏</b>：本端点仅 ADMIN 可访问，属 data-masking.md 允许的
 * 「独立权限层下可看明文」出口——用户管理场景若掩名（张*）将无法辨识与核对，
 * 反而不满足等保审计可读性要求。</p>
 */
@Data
public class SystemUserItem implements Serializable {

    /** 用户 id */
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 真实姓名（ADMIN 端点，明文出口） */
    private String realName;

    /** 角色标识（引用 sys_role.role_code） */
    private String roleCode;

    /** 角色中文名（来自 sys_role，便于列表直读） */
    private String roleName;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 可访问防区（逗号串；空=未分派） */
    private String zoneCodes;

    /** 是否需强制修改口令（管理员重置密码后为 true） */
    private Boolean mustChangePwd;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
