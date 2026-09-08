package com.sinopec.mmsecurity.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sinopec.mmsecurity.common.mask.Masked;
import com.sinopec.mmsecurity.common.mask.MaskingSerializer;
import com.sinopec.mmsecurity.common.mask.MaskType;
import lombok.Data;

import java.io.Serializable;

/**
 * 值班成员，与前端 {@code emergency.openapi.json#/DutyMember} 对齐。
 */
@Data
public class DutyMember implements Serializable {

    /** 成员唯一 ID */
    private String id;
    /** 姓名（响应出口脱敏：张*） */
    @JsonSerialize(using = MaskingSerializer.class)
    @Masked(MaskType.NAME)
    private String name;
    /** 联系电话（响应出口脱敏：138****8000） */
    @JsonSerialize(using = MaskingSerializer.class)
    @Masked(MaskType.PHONE)
    private String phone;
    /** 角色（值班领导/值班员） */
    private String role;
    /** 所属部门 */
    private String department;
    /** 班次（白班/夜班） */
    private String shift;
}
