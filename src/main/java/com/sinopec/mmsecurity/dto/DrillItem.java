package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 演练项（列表行），对齐 docs/api/drills.openapi.json#/components/schemas/DrillItem。 */
@Data
public class DrillItem {

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

    /** 参与部门（顿号分隔）。 */
    private String departments;

    /** 演练任务数。 */
    private Integer taskCount;
}
