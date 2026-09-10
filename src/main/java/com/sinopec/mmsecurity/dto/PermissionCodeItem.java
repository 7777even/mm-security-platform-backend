package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 权限码字典项（GET /system/permissions 出参）。
 * 由 sys_menu 中 perm_code 非空的节点聚合去重，供角色授权界面展示与前后端权限码核对。
 */
@Data
public class PermissionCodeItem implements Serializable {

    /** 权限码（如 system:user:create） */
    private String permCode;

    /** 权限中文名（取节点名称） */
    private String name;

    /** 所属节点 id */
    private Long menuId;
}
