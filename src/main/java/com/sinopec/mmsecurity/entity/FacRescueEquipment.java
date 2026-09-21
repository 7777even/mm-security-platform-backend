package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急救援资源 - 救援装备（对应表 fac_rescue_equipment）。
 * <p>救援力量唯一真源：装备以本表为准（V62 起「消防队伍详情」亦由本表按中队归组，
 * 装备总量 = 本表条数）。
 * V62 补充 category / unit 两列（原仅存在于已退役的 fac_brigade_equipment）。
 */
@Data
@TableName(value = "fac_rescue_equipment")
public class FacRescueEquipment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String equipName;

    private String squadron;

    /** 装备类别：防护装备 / 灭火器材 / 破拆工具 / 侦检仪器 / 通讯设备 / 照明排烟（队伍详情分组用）。 */
    private String category;

    /** 计量单位：套 / 盘 / 具 / 支 / 台 / 条 / 个。 */
    private String unit;

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
