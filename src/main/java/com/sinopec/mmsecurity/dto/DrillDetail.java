package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 演练详情（含任务子项），对齐 docs/api/drills.openapi.json#/components/schemas/DrillDetail。 */
@Data
public class DrillDetail {

    private Long id;

    /** 演练编号。 */
    private String drillCode;

    private String name;

    /** 演练类型。 */
    private String drillType;

    /** 演练形式。 */
    private String form;

    /** 演练时间。 */
    private String timeRange;

    private String place;

    /** 演练状态。 */
    private String status;

    /** 参与部门。 */
    private String departments;

    /** 演练任务子项列表。 */
    private List<DrillTaskItem> tasks;
}
