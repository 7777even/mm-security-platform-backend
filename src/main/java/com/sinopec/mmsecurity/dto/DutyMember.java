package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 值班成员，与前端 {@code emergency.openapi.json#/DutyMember} 对齐。
 */
@Data
public class DutyMember implements Serializable {

    /** 成员唯一 ID */
    private String id;
    /** 姓名 */
    private String name;
    /** 联系电话 */
    private String phone;
    /** 角色（值班领导/值班员） */
    private String role;
    /** 所属部门 */
    private String department;
    /** 班次（白班/夜班） */
    private String shift;
}
