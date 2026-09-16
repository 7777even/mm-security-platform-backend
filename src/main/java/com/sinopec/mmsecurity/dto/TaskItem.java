package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 处置任务项（对齐 docs/api/tasks.openapi.json#/components/schemas/TaskItem）。 */
@Data
public class TaskItem {

    private Long id;

    /** 任务编号（对外展示）。 */
    private String taskCode;

    private String title;

    /** 级别（紧急/重要/一般）。 */
    private String level;

    /** 任务来源。 */
    private String source;

    /** 任务区域 / 位置。 */
    private String area;

    private String deadline;

    private String status;

    private String description;
}
