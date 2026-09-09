package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急预案 - 子阶段行（挂在大阶段下，携带完成进度）。 */
@Data
public class PlanSubPhase {
    private String id;
    private String parentId;
    private String name;
    private Integer order;
    private Integer progress;
}
