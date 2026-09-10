package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-菜单/权限授权（纯关联表，无逻辑删除列）。
 *
 * <p>撤销授权采用**硬删**（见 design §5.1 / Q4 裁决）：该表不承载业务身份，
 * 若带 deleted 会与「删后再授」的唯一约束冲突，且 MyBatis-Plus 全局逻辑删除
 * 会干扰等值查询。故本实体**不带 deleted 字段**，保持自然语义。</p>
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("role_id")
    private Long roleId;

    @TableField("menu_id")
    private Long menuId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
