package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 处置任务列表（对齐 docs/api/tasks.openapi.json#/components/schemas/TaskList）。 */
@Data
public class TaskList {

    private List<TaskItem> items;

    /** 任务总数。 */
    private Integer total;
}
