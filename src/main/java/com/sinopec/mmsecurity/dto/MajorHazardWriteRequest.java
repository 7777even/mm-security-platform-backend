package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 重大危险源写请求（新建/更新共用）。
 *
 * <p>id 由服务端按 {@code LedgerIdSupport} 分配（规避 H2/PG/DM 自增序列滞后撞主键）。
 * 仅覆盖主数据列：嵌套明细（联系人 / 档案 / 监测点 / 视频 / 化学品 / 疏散路线 / 应急操作）
 * 以 JSON 列承载、由 seeder 维护，本批写端点不开放编辑。
 */
@Data
public class MajorHazardWriteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String name;

    /** 危险等级（一/二/三/四级）。 */
    private String level;

    /** 重大危险源 R 值（定量风险评价）。 */
    private Double rValue;

    private Integer monitorCount;

    private Integer videoCount;

    private String enterprise;

    private String category;

    /** 危险源编码（业务自然键）。 */
    private String code;

    private Double longitude;

    private Double latitude;
}
