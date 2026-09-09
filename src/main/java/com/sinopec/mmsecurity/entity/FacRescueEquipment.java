package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 救援装备（对应 H2 表 fac_rescue_equipment）。
 * 来源：rescueEquipmentMock.ts 的 buildItem 生成结果，装备总量 375 套另取常量。
 */
@Data
@TableName(value = "fac_rescue_equipment")
public class FacRescueEquipment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String equipName;

    private String squadron;

    private Integer quantity;

    private String leaderName;

    private String leaderPhone;

    private Integer stockQuantity;

    private String equipModel;

    private String protectionType;

    private String filterCanister;

    private String maxContinuousUse;

    private String storageLocation;

    private String purchaseBatch;

    private String factoryValidityYears;

    private String remainingValidity;

    private String lastInspectionDate;

    private String nextMandatoryMaintenanceDate;

    private String equipmentStatus;

    private String scrapWarning;

    private String issueRegistration;

    private String spareParts;

    private Integer sortNo;
}
