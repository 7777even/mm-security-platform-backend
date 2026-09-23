package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消防故障写回请求：状态流转（faultStatus）+ 派单/维修/验收字段局部更新 + 时间线追加。
 *
 * <p>字段均为可选——前端按实际发生的状态机跃迁局部更新，未涉及的字段不传（read-modify-write）。
 * 故障状态枚举（与 sys_dict_item 字典 {@code fire_facility_fault_status} 对齐）：
 * 待确认 → 已确认 → 已派单 → 维修中 → 待验收 → 已闭环。</p>
 */
@Data
public class FireFacilityFaultUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 故障状态。不传则不更新状态。 */
    private String faultStatus;

    /** 工单号（派单时生成）。不传则不更新。 */
    private String workOrderNo;

    /** 维修人/派单人员。不传则不更新。 */
    private String repairPerson;

    /** 预计完成时间（格式 yyyy-MM-dd HH:mm:ss）。不传则不更新。 */
    private String estimatedFinish;

    /** 实际完成时间（格式 yyyy-MM-dd HH:mm:ss）。不传则不更新。 */
    private String actualFinish;

    /** 维修措施说明。不传则不更新。 */
    private String repairMeasures;

    /** 验收人。不传则不更新。 */
    private String acceptancePerson;

    /** 验收结论（如 合格/不合格）。不传则不更新。 */
    private String acceptanceResult;

    /** 随本次写回追加的故障时间线（可选，支持一次追加多条）。 */
    private List<FireFacilityFaultTimelineCreate> timelines;
}
