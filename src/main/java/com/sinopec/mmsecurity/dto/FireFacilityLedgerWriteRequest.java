package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消防设施台账写请求：管理端（mgmt）台账录入/编辑用，落库 fac_fire_facility_ledger。
 *
 * <p>字段名对齐只读 DTO {@link FireFacilityLedgerItem}（location→location_name、device→device_name、
 * enabled→enabled_flag 在 Service 层映射），不在请求里暴露实体列名。
 * 所有字段可选（read-modify-write）：新增时缺失字段按默认值处理，编辑时仅覆盖传入字段。</p>
 */
@Data
public class FireFacilityLedgerWriteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 设施编码（新增必填，作为台账自然键，全局建议唯一）。 */
    private String facilityCode;

    /** 设施名称（新增必填）。 */
    private String facilityName;

    /** 设施类型（如 消防水泵 / 火灾报警控制器 / 自动喷淋系统）。 */
    private String facilityType;

    /** 设置部位（location_name）。 */
    private String location;

    /** 关联设备型号（device_name）。 */
    private String device;

    /** 维保人姓名。 */
    private String maintainerName;

    /** 维保人电话。 */
    private String maintainerPhone;

    /** 是否启用（enabled_flag），不传默认 true。 */
    private Boolean enabled;
}
