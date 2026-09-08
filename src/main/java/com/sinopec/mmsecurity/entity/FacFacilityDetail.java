package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 设施档案明细实体。基础字段/化学品字段/档案文件以 JSON 列承载，
 * 由 {@code HazardService} 解析为 FacilityDetailInfo 的 List&lt;Map&gt;。
 */
@Data
@TableName("fac_facility_detail")
public class FacFacilityDetail {
    private String facilityName;
    private String hazardSourceCode;
    private String basicFieldsJson;
    private String chemicalFieldsJson;
    private String archivesJson;
}
