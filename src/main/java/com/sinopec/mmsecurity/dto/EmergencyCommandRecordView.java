package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应急指令记录视图：落库后的完整行，含服务端填充的 {@code prevStatus} / {@code operator} / 时间戳。
 *
 * <p>契约 {@code emergency.openapi.json#/components/schemas/EmergencyCommandRecordView}。</p>
 */
@Data
public class EmergencyCommandRecordView implements Serializable {

    private Long id;
    private String commandCode;
    private String commandName;
    private String commandKind;
    /** 推进前状态（首次下发为空） */
    private String prevStatus;
    private String currStatus;
    private String dispatchMode;
    private String target;
    private String remark;
    private String operator;
    private LocalDateTime createdAt;
}
