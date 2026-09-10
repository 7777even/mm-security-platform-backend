package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 节点完成判据项，与前端 {@code criteriaChecklist[]} 对齐（契约 #/CriteriaChecklistItem）。 */
@Data
public class CriteriaChecklistItem implements Serializable {

    /** 判据 id */
    private String id;

    /** 判据文案 */
    private String label;

    /** 是否已勾选 */
    private Boolean checked;
}
