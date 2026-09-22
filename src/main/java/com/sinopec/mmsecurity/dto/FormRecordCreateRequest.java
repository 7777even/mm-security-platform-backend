package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 流程填报新增请求。
 *
 * <p>必填：formType（填报类型）/ reporter（填报人）/ content（填报内容）。
 * 选填：formNo（缺省后端生成）/ title / department / fillAt（缺省取当前时间）/ status（缺省 SUBMITTED）/ remark。
 */
@Data
public class FormRecordCreateRequest {

    /** 填报编号（可选，缺省后端生成）。 */
    private String formNo;

    /** 填报类型（必填）。 */
    private String formType;

    /** 填报标题（可选）。 */
    private String title;

    /** 填报人（必填）。 */
    private String reporter;

    /** 填报部门（可选）。 */
    private String department;

    /** 填报时间（可选，缺省取当前时间，VARCHAR 原样存）。 */
    private String fillAt;

    /** 结构化填报内容（必填，JSON 字符串，按 formType 维度组织）。 */
    private String detailJson;

    /** 状态（可选，缺省 SUBMITTED）。 */
    private String status;

    /** 备注（可选）。 */
    private String remark;
}
