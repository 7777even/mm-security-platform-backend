package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 应急救援资源 - 救援装备条目。
 * 字段名与前端 rescueEquipmentMock.ts 的 RescueEquipmentItem 完全一致。
 */
@Data
public class RescueEquipmentItem {
    private Long id;
    private String name;
    private String squadron;
    /** 装备类别（防护装备 / 堵漏器材 等）；与 RescueEquipmentWriteRequest.category 同源。 */
    private String category;
    /** 计量单位（具 / 套 / 吨 等）。 */
    private String unit;
    private Integer quantity;
    private String leaderName;
    private String leaderPhone;
    /** 在库数量 */
    private Integer stockQuantity;
    /** 装备规格 */
    private String model;
    private String protectionType;
    private String filterCanister;
    private String maxContinuousUse;
    private String storageLocation;
    private String purchaseBatch;
    private String factoryValidityYears;
    private String remainingValidity;
    /** 运维管理 */
    private String lastInspectionDate;
    private String nextMandatoryMaintenanceDate;
    private String equipmentStatus;
    private String scrapWarning;
    private String issueRegistration;
    private String spareParts;
}
