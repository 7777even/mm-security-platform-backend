package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 演练列表，对齐 docs/api/drills.openapi.json#/components/schemas/DrillList。 */
@Data
public class DrillList {

    private List<DrillItem> items;

    /** 演练总数。 */
    private Integer total;
}
