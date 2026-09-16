package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 演练任务子项，对齐 docs/api/drills.openapi.json#/components/schemas/DrillTaskItem。 */
@Data
public class DrillTaskItem {

    /** 任务名称。 */
    private String name;

    /** 任务状态。 */
    private String status;
}
