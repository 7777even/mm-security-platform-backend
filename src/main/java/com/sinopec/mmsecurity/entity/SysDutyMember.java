package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 应急值班值守成员（真实数据源，替代原 EmergencyService 硬编码常量）。 */
@Data
@TableName("sys_duty_member")
public class SysDutyMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String phone;
    private String role;
    private String department;
    private String shift;
}
