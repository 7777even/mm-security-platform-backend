package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 一键调用预案结果（含激活态、调用次数与留痕信息）。 */
@Data
public class PlanInvokeResult {
    private Long planId;
    private String planName;
    private String domain;
    /** 调用后是否激活（同域内唯一）。 */
    private Boolean isActive;
    /** 调用后累计次数。 */
    private Integer invokeCount;
    /** 本次调用时间。 */
    private LocalDateTime invokedAt;
    /** 操作人（来自登录态）。 */
    private String operator;
}
