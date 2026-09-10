package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 应急流程阶段，与前端 {@code EmergencyPhase} 对齐（契约 emergency.openapi.json#/EmergencyPhase）。 */
@Data
public class EmergencyPhase implements Serializable {

    /** 阶段编码（phase-team / phase-plant / phase-company / phase-gov / phase-close） */
    private String id;

    /** 阶段名称 */
    private String name;

    /** 起始节点号（含） */
    private Integer start;

    /** 结束节点号（含） */
    private Integer end;

    /** 阶段配色（blue/cyan/amber/red/green） */
    private String tone;
}
