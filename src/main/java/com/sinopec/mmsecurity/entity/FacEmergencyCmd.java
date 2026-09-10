package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 应急指挥指令（真实数据源，替代原 EmergencyService 硬编码常量）。 */
@Data
@TableName("fac_emergency_cmd")
public class FacEmergencyCmd {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private String grpId;
    private String grpLabel;
    private String grpTab;
    private String instructionType;
    private String name;
    private String location;
    private String status;
    private String actionLabel;
    private Boolean done;
    private String detailJson;
}
