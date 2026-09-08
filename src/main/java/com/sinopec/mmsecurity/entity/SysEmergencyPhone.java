package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 应急电话通讯录（真实数据源，替代原 EmergencyService 硬编码常量）。 */
@Data
@TableName("sys_emergency_phone")
public class SysEmergencyPhone {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String number;
    private String category;
}
