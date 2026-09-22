package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程填报记录项 DTO（列表行 + 详情共用）。
 *
 * <p>与前端 {@code form-records.openapi.json#/FormRecordItem} 字节级对齐；schema 名须与后端 DTO 类名一致，
 * 供 check-api-contract 守门脚本逐字段对拍。数据来自真实表 fac_form_record。
 */
@Data
public class FormRecordItem implements Serializable {

    private Long id;

    /** 填报编号（业务唯一展示号）。 */
    private String formNo;

    /** 填报类型（隐患排查 / 设备巡检 / 值班交接 / 其他）。 */
    private String formType;

    /** 填报标题。 */
    private String title;

    /** 填报人。 */
    private String reporter;

    /** 填报部门。 */
    private String department;

    /** 填报时间（VARCHAR 原样存，格式 yyyy-MM-dd HH:mm:ss）。 */
    private String fillAt;

    /** 结构化填报内容（JSON 字符串，按 formType 维度组织）。 */
    private String detailJson;

    /** 状态：DRAFT 草稿 / SUBMITTED 已提交 / REVIEWED 已审核。 */
    private String status;

    /** 备注。 */
    private String remark;

    /** 乐观锁版本号。 */
    private Long version;
}
