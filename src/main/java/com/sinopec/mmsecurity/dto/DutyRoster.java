package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应急值班值守表，与前端 {@code emergency.openapi.json#/DutyRoster} 对齐。
 * 属应急资源静态参考配置（部门/班次/成员）。
 */
@Data
public class DutyRoster implements Serializable {

    /** 可切换的部门列表（含「全部」） */
    private List<String> departments;
    /** 当前班次（白班/夜班） */
    private String shift;
    /** 值班成员列表 */
    private List<DutyMember> members;
}
