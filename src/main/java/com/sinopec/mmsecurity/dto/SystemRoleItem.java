package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 角色列表项 / 详情（GET /system/roles 系列出参）。 */
@Data
public class SystemRoleItem implements Serializable {

    /** 角色 id */
    private Long id;

    /** 角色标识（唯一） */
    private String roleCode;

    /** 角色中文名 */
    private String roleName;

    /** 角色说明 */
    private String description;

    /** 数据范围（ALL/DEPT/SELF，本期仅登记不参与过滤） */
    private String dataScope;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 是否内置角色（内置禁删、禁改 roleCode、禁停用） */
    private Boolean builtIn;

    /** 排序值 */
    private Integer sortOrder;

    /** 该角色下的用户数（用于删除保护的界面提示） */
    private Long userCount;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
