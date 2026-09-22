package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 流程填报更新请求（局部更新）。
 *
 * <p>所有字段均可选——前端按实际发生的编辑/状态流转局部更新，未涉及的字段不传。
 * 状态流转：DRAFT 草稿 / SUBMITTED 已提交 / REVIEWED 已审核。
 * version 为乐观锁版本号（来自列表/详情项），不传则不参与并发校验。
 */
@Data
public class FormRecordUpdateRequest {

    /** 填报类型。不传则不更新。 */
    private String formType;

    /** 标题。不传则不更新。 */
    private String title;

    /** 填报人。不传则不更新。 */
    private String reporter;

    /** 填报部门。不传则不更新。 */
    private String department;

    /** 填报时间。不传则不更新。 */
    private String fillAt;

    /** 结构化填报内容。不传则不更新。 */
    private String detailJson;

    /** 状态流转：DRAFT / SUBMITTED / REVIEWED。不传则不更新。 */
    private String status;

    /** 备注。不传则不更新。 */
    private String remark;

    /** 乐观锁版本号（来自详情/列表项）。不传则不参与并发校验。 */
    private Long version;
}
