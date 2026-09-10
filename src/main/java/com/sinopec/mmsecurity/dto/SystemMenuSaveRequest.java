package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 新增 / 修改菜单权限节点入参（POST /system/menus、PUT /system/menus/{id}）。 */
@Data
public class SystemMenuSaveRequest implements Serializable {

    /** 父节点 id（0 为顶层） */
    private Long parentId;

    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称长度不能超过 64")
    private String name;

    /** 节点编码（唯一；fm-* 须与前端路由 key 对齐） */
    @NotBlank(message = "节点编码不能为空")
    @Size(max = 128, message = "节点编码长度不能超过 128")
    private String code;

    /** 前端路由路径 */
    @Size(max = 255, message = "路径长度不能超过 255")
    private String path;

    /** 图标标识 */
    @Size(max = 64, message = "图标长度不能超过 64")
    private String icon;

    /** 排序值，缺省 0 */
    private Integer sortOrder;

    /** 节点类型：DIR / MENU / BUTTON */
    @NotBlank(message = "节点类型不能为空")
    private String menuType;

    /** 权限标识（如 system:user:create） */
    @Size(max = 128, message = "权限标识长度不能超过 128")
    private String permCode;

    /** 1 显示 / 0 隐藏，缺省 1 */
    private Integer visible;

    /** 1 启用 / 0 停用，缺省 1 */
    private Integer status;
}
