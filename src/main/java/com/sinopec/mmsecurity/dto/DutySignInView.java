package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 值班签到记录视图：落库后的完整行，含服务端填充的 {@code signTime} / {@code operator}。
 *
 * <p>契约 {@code emergency.openapi.json#/components/schemas/DutySignInView}。</p>
 */
@Data
public class DutySignInView implements Serializable {

    private Long id;
    private String dutyDate;
    private String shiftName;
    private String department;
    private String personName;
    private String signAction;
    /** 签到时间 YYYY-MM-DD HH:mm:ss（请求为空时由服务端按当前时间填充） */
    private String signTime;
    private String remark;
    private String operator;
    private LocalDateTime createdAt;
}
