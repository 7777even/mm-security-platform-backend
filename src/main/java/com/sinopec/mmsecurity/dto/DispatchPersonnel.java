package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急派单人员项，与前端 {@code emergency.openapi.json#/DispatchPersonnel} 对齐。
 * 供告警详情「派单人员」下拉使用。
 */
@Data
public class DispatchPersonnel implements Serializable {

    /** 人员 ID */
    private Long id;
    /** 姓名 */
    private String name;
    /** 岗位：值班领导 / 消防队长 / 工艺处置组长 … */
    private String role;
    /** 所属部门 */
    private String department;
    /** 联系电话 */
    private String phone;
}
