package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 防火巡查检查项，与前端
 * {@code fire-monitoring.openapi.json#/components/schemas/FirePatrolCheckItem} 对齐。
 */
@Data
public class FirePatrolCheckItem implements Serializable {

    /** 检查项编号（A1 / A2 / B1 … D6） */
    private String itemCode;
    /** 检查项分类 */
    private String category;
    /** 检查内容描述 */
    private String content;
    /** 检查结果（正常 / 异常 / 不适用） */
    private String result;
    /** 异常描述，仅 result=异常 时有值 */
    private String abnormalDesc;
    /** 异常现场照片文件名，仅 result=异常 时有值 */
    private String photoFile;
}
