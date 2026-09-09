package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 应急救援资源 - 消防队伍人员。
 *
 * <p>group 不是 Java 关键字，可作为属性名；实体侧列名与属性名为 person_group / personGroup。
 */
@Data
public class FireBrigadePerson {
    private Long id;
    private String name;
    private String role;
    private String group;
    private String phone;
    private String dutyStatus;
}
