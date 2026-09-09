package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急预案 - 作战力量/资源行；expectedCount / actualCount 统一按字符串输出。 */
@Data
public class PlanCombatResource {
    private String id;
    private String name;
    private String expectedCount;
    private String actualCount;
    private String leaderName;
    private String contactPhone;
    private String duties;
    private Double lon;
    private Double lat;
}
