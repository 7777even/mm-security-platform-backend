package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 救援装备（应急物资）新增 / 编辑入参，字段名对齐 {@link RescueEquipmentItem}。
 *
 * <p><b>新增语义</b>：{@code name} 必填（service 校验）；{@code sortNo} 取当前最大值 +1。
 * <b>编辑语义</b>：局部更新，字段为 {@code null} 表示不修改。</p>
 *
 * <p>数量类字段（quantity / stockQuantity）为整数，单位由 {@code unit} 描述
 * （如 具 / 套 / 吨）；二者语义不同——前者是编配数量、后者是当前库存，分开维护。</p>
 */
@Data
public class RescueEquipmentWriteRequest {

    /** 装备名称 */
    private String name;

    /** 所属中队 */
    private String squadron;

    /** 装备类别（如 防护装备 / 堵漏器材） */
    private String category;

    /** 计量单位 */
    private String unit;

    /** 编配数量 */
    private Integer quantity;

    /** 责任人姓名 */
    private String leaderName;

    /** 责任人电话 */
    private String leaderPhone;

    /** 当前库存数量 */
    private Integer stockQuantity;

    /** 规格型号 */
    private String model;

    /** 防护类型 */
    private String protectionType;

    /** 滤毒罐型号 */
    private String filterCanister;

    /** 最长连续使用时长 */
    private String maxContinuousUse;

    /** 存放位置 */
    private String storageLocation;

    /** 采购批次 */
    private String purchaseBatch;

    /** 出厂有效期（年） */
    private String factoryValidityYears;

    /** 剩余有效期 */
    private String remainingValidity;

    /** 上次检查日期 */
    private String lastInspectionDate;

    /** 下次强制维护日期 */
    private String nextMandatoryMaintenanceDate;

    /** 装备状态（如 完好 / 待修 / 报废） */
    private String equipmentStatus;

    /** 报废预警 */
    private String scrapWarning;

    /** 领用登记 */
    private String issueRegistration;

    /** 备品备件 */
    private String spareParts;
}
