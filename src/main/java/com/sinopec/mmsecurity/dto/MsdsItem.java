package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 化学品项（列表行），对齐 docs/api/msds.openapi.json#/components/schemas/MsdsItem。 */
@Data
public class MsdsItem {

    private Long id;

    /** 化学品名称。 */
    private String name;

    /** CAS 号。 */
    private String cas;

    /** 危险性分类。 */
    private String classification;
}
