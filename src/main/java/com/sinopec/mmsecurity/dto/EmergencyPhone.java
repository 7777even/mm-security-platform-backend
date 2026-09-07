package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急电话通讯录条目，与前端 {@code emergency.openapi.json#/EmergencyPhone} 对齐。
 */
@Data
public class EmergencyPhone implements Serializable {

    /** 通讯录条目 ID */
    private String id;
    /** 名称/单位 */
    private String name;
    /** 联系电话 */
    private String number;
    /** 分类：消防/医疗/公安/厂内应急/保卫值班/应急通讯/智能联动 */
    private String category;
}
