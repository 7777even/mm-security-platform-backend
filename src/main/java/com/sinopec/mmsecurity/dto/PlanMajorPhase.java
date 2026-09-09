package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急预案 - 大阶段行（下方展示上报与升级流程）。 */
@Data
public class PlanMajorPhase {
    private String id;
    private String name;
    private Integer order;
    private String upgradeProcess;
}
