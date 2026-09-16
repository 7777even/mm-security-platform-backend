package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 化学品 MSDS 详情，对齐 docs/api/msds.openapi.json#/components/schemas/MsdsDetail。 */
@Data
public class MsdsDetail {

    private Long id;

    /** 化学品名称。 */
    private String name;

    /** CAS 号。 */
    private String cas;

    /** 危险性分类。 */
    private String classification;

    /** 物理状态。 */
    private String state;

    /** 沸点。 */
    private String boilingPoint;

    /** 闪点。 */
    private String flashPoint;

    /** 爆炸极限。 */
    private String explosionLimit;

    /** 储存要求。 */
    private String storage;

    /** 安全措施。 */
    private String safety;

    /** 应急处置。 */
    private String emergency;
}
