package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 角色菜单授权入参（PUT /system/roles/{id}/menus）：整表覆盖语义。 */
@Data
public class SystemRoleMenuAssign implements Serializable {

    /** 授权菜单/权限节点 id 集合（全量覆盖；空数组表示回收该角色全部授权） */
    private List<Long> menuIds;
}
