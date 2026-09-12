package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 巡更执行上报写请求（A2 业务写侧）。
 *
 * <p>巡查计划本体（{@code GET /fire/patrols}）仍是只读班次记录，
 * 实际执行结果落独立的 V47 {@code fac_patrol_execution} 表。</p>
 *
 * <p>契约 {@code fire-monitoring.openapi.json#/components/schemas/PatrolExecutionWriteRequest}。</p>
 */
@Data
public class PatrolExecutionWriteRequest implements Serializable {

    /** 巡查日期 YYYY-MM-DD，必填 */
    private String patrolDate;

    /** 班次（上午 / 下午 / 夜间） */
    private String shiftName;

    /** 巡查责任人姓名，必填 */
    private String dutyPerson;

    /** 当班第几次巡查，如「第1次」 */
    private String patrolCount;

    /** 本次巡查部位 */
    private String location;

    /** 执行结果：NORMAL 正常 / ABNORMAL 异常，必填 */
    private String execResult;

    /** 异常描述（正常时为空） */
    private String finding;

    /** 派单后关联的工单号，可为空 */
    private String workOrderNo;
}
