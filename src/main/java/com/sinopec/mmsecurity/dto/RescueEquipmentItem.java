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
