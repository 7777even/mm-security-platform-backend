package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急派单人员名册（对应 H2 表 fac_dispatch_personnel，V35 建表）。
 * 用于告警详情「派单人员」下拉——替代前端 AlarmDetailPanel 硬编码的 5 个人名。
 * 不复用 sys_duty_member（出参经脱敏）与 fac_rescue_personnel（375 人，过多不适合下拉）。
 */
@Data
@TableName(value = "fac_dispatch_personnel")
public class FacDispatchPersonnel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 姓名（不脱敏，派单需要可辨识） */
    private String personName;

    /** 岗位：值班领导 / 消防队长 / 工艺处置组长 … */
    private String dutyRole;

    /** 所属部门 */
    private String department;

    /** 联系电话 */
    private String phone;

    private Integer sortNo;

    /** 1 启用 / 0 停用 */
    private Integer status;
}
