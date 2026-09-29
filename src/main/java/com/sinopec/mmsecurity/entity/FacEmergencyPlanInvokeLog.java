package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应急预案一键调用留痕实体（对应 H2 表 fac_emergency_plan_invoke_log，V85）。
 *
 * <p>「一键调用」语义 = 激活 + 广播 + 留痕，仅更新预案状态与留痕，不向任何物理设备下发
 * 控制指令（零下行控制红线）。本表记录每次调用的预案、域、操作人与备注，供审计追溯。</p>
 */
@Data
@TableName(value = "fac_emergency_plan_invoke_log")
public class FacEmergencyPlanInvokeLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private String planName;

    private String domain;

    private String operator;

    private String invokeNote;

    private LocalDateTime invokeAt;
}
