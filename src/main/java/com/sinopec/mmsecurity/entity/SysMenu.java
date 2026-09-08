package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统菜单（顶部导航）。id 自增；menuKey 映射 V1 物理列 {@code code}（与前端 MENU_ROUTE_SPECS 的
 * 字符串 key 对齐，如 fm-fire）；sort 映射 {@code sort_order}；allowedRoles 为本迁移新增的 RBAC 角色列。
 * parent_id / icon / status / deleted 列由 V1 定义，本实体不映射（取库默认值）。
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("code")
    private String menuKey;

    private String name;
    private String path;

    @TableField("sort_order")
    private Integer sort;

    @TableField("allowed_roles")
    private String allowedRoles;
}
