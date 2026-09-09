package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 值班人员，与 typhoon-emergency.openapi.json#/TyphoonDutyPerson 对齐。
 */
@Data
public class TyphoonDutyPerson implements Serializable {

    /** 人员 id */
    private Long id;
    /** 姓名 */
    private String name;
    /** 值班角色 */
    private String role;
    /** 联系电话 */
    private String phone;
    /** 头像序号 */
    private Integer avatarIndex;
}
