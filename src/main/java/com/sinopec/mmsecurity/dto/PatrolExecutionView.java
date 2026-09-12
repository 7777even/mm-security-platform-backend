package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 巡更执行记录视图：落库后的完整行，含服务端填充的 {@code operator} / 时间戳。
 *
 * <p>契约 {@code fire-monitoring.openapi.json#/components/schemas/PatrolExecutionView}。</p>
 */
@Data
public class PatrolExecutionView implements Serializable {

    private Long id;
    private String patrolDate;
    private String shiftName;
    private String dutyPerson;
    private String patrolCount;
    private String location;
    private String execResult;
    private String finding;
    private String workOrderNo;
    private String operator;
    private LocalDateTime createdAt;
}
