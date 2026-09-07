package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应急电话通讯录，与前端 {@code emergency.openapi.json#/EmergencyPhoneBook} 对齐。
 * 属应急资源静态参考配置（消防/医疗/公安等联络方式）。
 */
@Data
public class EmergencyPhoneBook implements Serializable {

    /** 通讯录条目列表 */
    private List<EmergencyPhone> entries;
}
