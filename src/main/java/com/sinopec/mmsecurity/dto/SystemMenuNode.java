package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单 / 权限节点（GET /system/menus 树形出参）。
 *
 * <p>{@code menuType} 为 DIR（目录）/ MENU（菜单）/ BUTTON（按钮权限）；
 * BUTTON 节点不参与导航装配，仅贡献 {@code permCode}。</p>
 */
@Data
public class SystemMenuNode implements Serializable {

    /** 节点 id */
    private Long id;

    /** 父节点 id（0 为顶层） */
    private Long parentId;

    /** 菜单名称 */
    private String name;

    /** 节点编码（fm-* 与前端路由 key 对齐；节点唯一） */
    private String code;

    /** 前端路由路径 */
    private String path;

    /** 图标标识 */
    private String icon;

    /** 排序值 */
    private Integer sortOrder;

    /** 节点类型：DIR / MENU / BUTTON */
    private String menuType;

    /** 权限标识（如 system:user:create），DIR 可为空 */
    private String permCode;

    /** 1 显示 / 0 隐藏 */
    private Integer visible;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 子节点 */
    private List<SystemMenuNode> children;
}
