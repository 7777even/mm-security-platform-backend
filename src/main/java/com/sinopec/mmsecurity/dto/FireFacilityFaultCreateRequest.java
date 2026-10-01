package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消防故障新增请求：管理端（mgmt）台账录入用，落库 fac_fire_facility_fault。
 *
 * <p>必填：faultCode（故障编号，唯一）、facilityCode（关联设施编码）、faultType（故障类型）、
 * faultLevel（故障级别）、discoverTime（发现时间）。其余字段可选，未传写空值。
 * 故障级别枚举（对齐既有种子数据与字典）：紧急 / 重要 / 一般。
 * 故障状态枚举（与 sys_dict_item 字典 {@code fire_facility_fault_status} 对齐）：
 * 待确认 → 已确认 → 已派单 → 维修中 → 待验收 → 已闭环；不传默认「待确认」。</p>
 */
@Data
public class FireFacilityFaultCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 故障编号（必填，全局唯一，如 FLT-2026-0001）。 */
    private String faultCode;

    /** 关联设施编码（必填）。 */
    private String facilityCode;

    /** 关联设施名称。 */
    private String facilityName;

    /** 设施类型（如 火灾自动报警系统 / 消火栓系统）。 */
    private String facilityType;

    /** 故障类型（必填：硬件故障 / 通信故障 / 误报 / 其他）。 */
    private String faultType;

    /** 故障级别（必填：紧急 / 重要 / 一般）。 */
    private String faultLevel;

    /** 发现时间（必填，格式 yyyy-MM-dd HH:mm:ss）。 */
    private String discoverTime;

    /** 发现方式（如 巡检发现 / 系统报警 / 人工上报）。 */
    private String discoverMethod;

    /** 故障现象描述。 */
    private String phenomenon;

    /** 故障原因（cause_text）。 */
    private String cause;

    /** 故障状态，不传默认「待确认」。 */
    private String faultStatus;

    /** 工单号。 */
    private String workOrderNo;

    /** 维修责任人。 */
    private String repairPerson;

    /** 预计完成时间（格式 yyyy-MM-dd HH:mm:ss）。 */
    private String estimatedFinish;

    /** 实际完成时间（格式 yyyy-MM-dd HH:mm:ss）。 */
    private String actualFinish;

    /** 维修措施说明。 */
    private String repairMeasures;

    /** 验收人。 */
    private String acceptancePerson;

    /** 验收结论（如 合格 / 不合格）。 */
    private String acceptanceResult;
}
