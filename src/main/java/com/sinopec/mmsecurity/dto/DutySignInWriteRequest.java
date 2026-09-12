package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急值班签到 / 签退写请求（A2 业务写侧）。
 *
 * <p>值班排班本体（{@code GET /emergency/duty}）仍是只读参考配置，签到动作落独立的
 * V47 {@code fac_duty_sign_in} 表，不改既有配置数据。</p>
 *
 * <p>契约 {@code emergency.openapi.json#/components/schemas/DutySignInWriteRequest}。</p>
 */
@Data
public class DutySignInWriteRequest implements Serializable {

    /** 值班日期 YYYY-MM-DD，必填 */
    private String dutyDate;

    /** 班次（白班 / 夜班） */
    private String shiftName;

    /** 值班部门 */
    private String department;

    /** 值班人员姓名，必填 */
    private String personName;

    /** 签到动作：SIGN_IN 签到 / SIGN_OUT 签退 */
    private String signAction;

    /** 备注说明 */
    private String remark;
}
