package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 应急指引实时值班表，与前端 {@code mockDutyRoster} 对齐（契约 #/GuidanceDutyRoster）。 */
@Data
public class GuidanceDutyRoster implements Serializable {

    /** 值班班组 */
    private String shiftGroup;

    /** 班组长（含岗位） */
    private String supervisor;

    private String supervisorPhone;

    /** 内操（含岗位） */
    private String boardOperator;

    private String boardOperatorPhone;

    /** 外操（含岗位） */
    private String fieldOperator;

    private String fieldOperatorPhone;
}
