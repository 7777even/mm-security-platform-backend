package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 处置任务（移动端任务中心真实数据源，替代 apps/mobile/data/mock.ts 的 tasks 静态数据）。 */
@Data
@TableName("fac_dispatch_task")
public class FacDispatchTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务编号（对外展示，如 TASK-001）。 */
    private String taskCode;

    private String title;

    /** 级别（紧急/重要/一般）。列名 task_level：达梦/Oracle 中 LEVEL 为保留字。 */
    @TableField("task_level")
    private String level;

    /** 任务来源（后台派发 / 巡查计划 / 故障工单）。 */
    private String source;

    /** 任务区域 / 位置。 */
    private String area;

    private String deadline;

    private String status;

    private String description;
}
