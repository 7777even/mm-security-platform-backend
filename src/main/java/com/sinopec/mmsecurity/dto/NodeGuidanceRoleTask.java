package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 指引岗位任务，与前端 {@code NodeGuidance.roleTasks[]} 对齐（契约 #/NodeGuidanceRoleTask）。 */
@Data
public class NodeGuidanceRoleTask implements Serializable {

    /** 岗位名（内操 / 外操 / 班长） */
    private String roleName;

    /** 岗位职责标题 */
    private String roleTitle;

    /** 值班人姓名 */
    private String personName;

    /** 头像图标 */
    private String avatarIcon;

    /** 联系电话 */
    private String phone;

    /** 该岗位本节点任务清单 */
    private List<String> tasks;
}
