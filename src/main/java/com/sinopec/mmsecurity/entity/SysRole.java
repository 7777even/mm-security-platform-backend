package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色（RBAC 授权主体）。role_code 为角色标识（如 ADMIN / SCHEDULER），
 * 由 sys_user.role 引用；菜单/权限授权经 sys_role_menu 关联 sys_menu。
 * data_scope 为数据范围预留列（ALL/DEPT/SELF），本期仅登记不参与过滤。
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色标识（唯一，忽略大小写比对） */
    @TableField("role_code")
    private String roleCode;

    /** 角色中文名 */
    @TableField("role_name")
    private String roleName;

    /** 角色说明 */
    private String description;

    /** 数据范围（ALL/DEPT/SELF，预留） */
    @TableField("data_scope")
    private String dataScope;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 1 内置角色（禁删除、禁改 role_code、禁停用） */
    @TableField("built_in")
    private Integer builtIn;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
