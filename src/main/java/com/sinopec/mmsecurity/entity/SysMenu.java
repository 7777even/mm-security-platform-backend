package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统菜单 / 权限节点（统一授权轴，ADR-2）。
 *
 * <p>id 自增；menuKey 映射物理列 {@code code}（与前端 MENU_ROUTE_SPECS 的字符串 key 对齐，如 fm-fire）；
 * sort 映射 {@code sort_order}。</p>
 *
 * <p>V32 起新增三列语义：{@code menu_type}（DIR 目录 / MENU 菜单 / BUTTON 按钮）、
 * {@code perm_code}（权限标识，如 system:user:create）、{@code visible}（1 显示 / 0 隐藏）。
 * BUTTON 节点不参与导航装配，仅贡献权限码。</p>
 *
 * <p>{@code allowed_roles} 为 V7 遗留的逗号分隔授权列，V33 已把授权迁移至
 * {@code sys_role_menu}；本列**降级为只读兼容列**（不再写入、不再参与判定），
 * 保留仅为可回退与历史对照。</p>
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("parent_id")
    private Long parentId;

    @TableField("code")
    private String menuKey;

    private String name;
    private String path;
    private String icon;

    @TableField("sort_order")
    private Integer sort;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 1 显示 / 0 隐藏（隐藏节点不参与导航，但可携带权限码） */
    private Integer visible;

    /** 节点类型：DIR 目录 / MENU 菜单 / BUTTON 按钮（权限码） */
    @TableField("menu_type")
    private String menuType;

    /** 权限标识，如 system:user:create；DIR 可为空 */
    @TableField("perm_code")
    private String permCode;

    private Integer deleted;

    /** V7 遗留授权列（降级只读，不参与判定） */
    @TableField("allowed_roles")
    private String allowedRoles;
}
