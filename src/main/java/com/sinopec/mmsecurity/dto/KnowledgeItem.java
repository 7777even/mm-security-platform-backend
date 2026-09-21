package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急安全知识条目，与前端 {@code emergency.openapi.json#/KnowledgeItem} 对齐。
 */
@Data
public class KnowledgeItem implements Serializable {

    /** 知识条目 ID */
    private String id;
    /** 标题 */
    private String title;
    /** 知识条目数 */
    private Integer count;
    /** 图标名（Element Plus icon 名） */
    private String icon;
    /** 知识分类说明（真实可编辑文案，来自 sys_knowledge_item.description） */
    private String description;
}
