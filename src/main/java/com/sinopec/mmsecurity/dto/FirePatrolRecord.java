package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 防火巡查记录，与前端
 * {@code fire-monitoring.openapi.json#/components/schemas/FirePatrolRecord} 对齐。
 */
@Data
public class FirePatrolRecord implements Serializable {

    /** 记录 id（主键） */
    private Long id;
    /** 巡查日期，格式 YYYY-MM-DD */
    private String patrolDate;
    /** 班次（上午 / 下午 / 夜间） */
    private String shift;
    /** 巡查责任人姓名 */
    private String dutyPerson;
    /** 当班第几次巡查，如「第1次」 */
    private String patrolCount;
    /** 本次巡查覆盖的部位列表（默认空列表，避免前端 List 渲染时出现 null） */
    private List<String> locations = new ArrayList<>();
    /** 是否已完成巡查 */
    private Boolean completed;
    /** 关联工单号，仅在发现异常并派单后有值 */
    private String workOrderNo;
    /** 15 项标准检查项逐条结果（默认空列表，避免前端 checkItems.some() 空指针） */
    private List<FirePatrolCheckItem> checkItems = new ArrayList<>();
}
