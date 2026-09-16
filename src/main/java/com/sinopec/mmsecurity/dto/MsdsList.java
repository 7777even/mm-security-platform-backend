package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 化学品 MSDS 列表，对齐 docs/api/msds.openapi.json#/components/schemas/MsdsList。 */
@Data
public class MsdsList {

    private List<MsdsItem> items;

    /** 化学品总数。 */
    private Integer total;
}
