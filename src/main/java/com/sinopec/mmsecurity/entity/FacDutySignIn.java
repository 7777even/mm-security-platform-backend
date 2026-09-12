package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 值班签到记录：签到 / 签退（A2 业务写侧，V47）。
 *
 * <p>应急值班值守表（DutyRoster）当前为静态参考配置（部门/班次/成员），
 * 签到动作落本表，不改既有配置数据。</p>
 */
@Data
@TableName("fac_duty_sign_in")
public class FacDutySignIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 值班日期 YYYY-MM-DD */
    private String dutyDate;
    /** 班次：白班 / 夜班 */
    private String shiftName;
    private String department;
    private String personName;
    /** 签到动作：SIGN_IN / SIGN_OUT */
    private String signAction;
    /** 签到时间 YYYY-MM-DD HH:mm:ss */
    private String signTime;
    private String remark;
    private String operator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
