package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急通讯录条目写请求（新增 / 编辑共用）。字段名对齐只读 DTO {@link EmergencyPhone}，
 * 前端 {@code openEdit} 可直接 {@code {...row}} 灌入表单，零字段映射。
 */
@Data
public class PhoneWriteRequest implements Serializable {

    /** 名称/单位（必填）。 */
    private String name;

    /** 联系电话（必填）。 */
    private String number;

    /** 分类（消防/医疗/公安/厂内应急/保卫值班/应急通讯/智能联动，可选）。 */
    private String category;
}
