package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 重大危险源主数据实体。嵌套明细（联系人/档案/监测点/视频/化学品/疏散路线/应急操作）以 JSON 列承载，
 * 由 {@code HazardService} 解析为 DTO 的 List&lt;Map&gt;，避免为演示数据爆炸出数十张子表。
 */
@Data
@TableName("fac_major_hazard")
public class FacMajorHazard {
    private Long id;
    private String name;
    private String level;
    private Double rValue;
    private Integer monitorCount;
    private Integer videoCount;
    private String enterprise;
    private String category;
    private String code;
    private Double longitude;
    private Double latitude;
    private String commissionDate;
    private Boolean keyProcess;
    private Boolean inChemicalPark;
    private String contactsJson;
    private String filesJson;
    private String monitorsJson;
    private String videosJson;
    private String chemicalsJson;
    private String evacuationRoutesJson;
    private String operationsJson;
}
